package com.csproj.Cyberlab.API.virtualization.proxmox;

import com.csproj.Cyberlab.API.exceptions.FullDiskException;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VncConnectionResponse;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;
import it.corsinvest.proxmoxve.api.PveClient;
import it.corsinvest.proxmoxve.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

//---------------------------------------------------------------
// Provides upstream virtualization services via Proxmox
//---------------------------------------------------------------
@Service
@Slf4j
public class ProxmoxClient implements VirtualizationProvider {
    // credit: https://github.com/Corsinvest/cv4pve-api-java/tree/master
    private final PveClient client;
    private final ProxmoxSftpService proxmoxSftpService;
    private final String node;
    private PveClient.PVENodes.PVENodeItem.PVEStorage stg;

    public static final int ROOT_VM_ID = 100;
    public static final int MIN_VM_TEMPLATE_ID = 101;
    public static final int MAX_VM_TEMPLATE_ID = 1000;
    public static final int MIN_VM_INSTANCE_ID = 1001;
    public static final int MAX_VM_INSTANCE_ID = 10000;

    public static final String DHCP_RANGE = "start-address=10.0.0.2,end-address=10.0.0.254";
    public static final String SUBNET = "10.0.0.0/24";
    public static final String DNS_SERVER = "10.0.0.1";
    public static final String GATEWAY = "10.0.0.1";
    public static final String NETWORK_DEVICE = "e1000";

    public ProxmoxClient(Environment env, ProxmoxSftpService proxmoxSftpService) {
        String hostname = env.getProperty("proxmox.api.hostname");
        int port = Integer.parseInt(env.getProperty("proxmox.api.port", "8006"));
        String node = env.getProperty("proxmox.api.node", "proxmove");
        String auth = env.getProperty("proxmox.api.auth");

        if (hostname == null || hostname.isEmpty() ||
                auth == null || auth.isEmpty() ||
                port < 0) {
            throw new IllegalArgumentException("Missing or invalid Proxmox connection args");
        }

        this.proxmoxSftpService = proxmoxSftpService;
        this.node = node;
        this.client = new PveClient(hostname, port);
        this.client.setApiToken(auth);
        this.stg = client.getNodes().get(node).getStorage();

        testConnection();
    }

    /**
     * Tests for successful connection with a sanity check
     */
    private void testConnection() {
        if (client.getNodes().get(node).index().isSuccessStatusCode()) {
            log.info("Proxmox connection established");
        } else {
            log.warn("Proxmox connection failed");
        }
    }

    /**
     * Finds the next available Proxmox VM ID within the valid bounds
     *
     * @param min Minimum ID inclusive
     * @param max Maximum ID inclusive
     * @return Next available Proxmox VM ID
     */
    private int getNextId(int min, int max) {
        // Get all IDs in use
        JSONArray vmList = client.getNodes().get(node).getQemu().vmlist().getResponse().getJSONArray("data");
        int nextId = min;

        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < vmList.length(); i++) {
            ids.add(vmList.getJSONObject(i).getInt("vmid"));
        }

        while (ids.contains(nextId)) {
            nextId++;
        }

        if (nextId > max) {
            throw new RuntimeException("No available VM IDs");
        }

        return nextId;
    }

    @Override
    public VmInstance createInstance(VmTemplate parentVm) {
        if (checkDiskSpace("images")) {
            throw new FullDiskException();
        }

        int newProxmoxId = getNextId(MIN_VM_INSTANCE_ID, MAX_VM_INSTANCE_ID);
        String name = "Clone-" + newProxmoxId;

        try {
            Result res = client.getNodes().get(node).getQemu().get(parentVm.getProxmoxId()).getClone().cloneVm(
                    newProxmoxId,
                    null,
                    "Cloned from VM template with id:" + parentVm.getProxmoxId(),
                    "qcow2",
                    true,
                    name,
                    null,
                    null,
                    null,
                    node
            );

            if (!res.isSuccessStatusCode()) {
                throw new RuntimeException("Failed to clone VM: " + res.getResponse().toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error cloning VM: " + e.getMessage());
        }

        return new VmInstance(newProxmoxId, parentVm.getProxmoxNode(), name, new Date(), parentVm.getId());
    }

    @Override
    public void deleteInstance(int proxmoxId) {
        Result result = client.getNodes().get(node).getQemu().get(proxmoxId).destroyVm();

        if (!result.isSuccessStatusCode()) {
            throw new RuntimeException("Failed to delete VM Instance from Proxmox: " + result.getResponse());
        }
    }

    @Override
    public void deleteTemplate(int proxmoxId) {
        Result result = client.getNodes().get(node).getQemu().get(proxmoxId).destroyVm();

        if (!result.isSuccessStatusCode()) {
            throw new RuntimeException("Failed to delete VM Template from Proxmox: " + result.getResponse());
        }
    }

    @Override
    public VmTemplate createTemplate(InputStream inputStream, String name, String description) throws FullDiskException {
        if (checkDiskSpace("images")) {
            throw new FullDiskException();
        }

        int newId = getNextId(MIN_VM_TEMPLATE_ID, MAX_VM_TEMPLATE_ID);

        // Upload VDI using random temp file name
        String filename = "upload" + Math.random() + ".vdi";
        proxmoxSftpService.uploadVdi(inputStream, filename);
        client.waitForTaskToFinish(client.getNodes().get(node).getQemu().get(ROOT_VM_ID).getClone().cloneVm(newId)
                .getResponse().getString("data"), 500, 10000);
        proxmoxSftpService.executeRemoteCommand(filename, newId);

        // Set configuration to use new hard drive, change name, etc.
        updateConfig(newId, null);

        return new VmTemplate(name, description, newId, node);
    }

    /**
     * Get Proxmox PVE ticket and VNC connection port number
     *
     * @param id Proxmox id of desired VM
     * @return VNC connection data
     */
    @Override
    public VncConnectionResponse getConnectionURI(String id) {
        JSONObject pveTicketData = client.getNodes().get(node).getQemu().get(id).getVncproxy().vncproxy().getResponse().getJSONObject("data");
        String pveTicketString = "PVEAuthCookie=" + pveTicketData.get("ticket").toString();
        String vncPort = pveTicketData.get("port").toString();

        return new VncConnectionResponse(
                String.format("/api2/json/nodes/%s/qemu/%s/vncwebsocket?port=%s&vncticket=%s", node, id, vncPort, pveTicketString), // @TODO: this will more than likely need to be URI encoded.
                pveTicketString,
                vncPort);
    }

    @Override
    public void getById() {
    }

    /**
     * Updates configuration of cloned VM
     *
     * @param vmId Proxmox ID of VM to update
     * @return boolean for successful config change
     */
    public boolean updateConfig(int vmId, Map<Integer, String> networkDevice) {
        // Retrieve current VM configuration
        JSONObject currentConfig = this.client.getNodes().get(node).getQemu().get(vmId).getConfig().vmConfig().getResponse();
        JSONObject data = currentConfig.getJSONObject("data");

        // Extract current values (or use defaults)
        Boolean acpi = data.has("acpi") ? data.getBoolean("acpi") : null;
        String affinity = data.optString("affinity", null);
        String agent = data.optString("agent", null);
        String amdSev = data.optString("amd_sev", null);
        String arch = data.optString("arch", null);
        String args = data.optString("args", null);
        String audio0 = data.optString("audio0", null);
        Boolean autostart = data.has("autostart") ? data.getBoolean("autostart") : null;
        Integer balloon = data.has("balloon") ? data.getInt("balloon") : null;
        String bios = data.optString("bios", null);
        String boot = "order=scsi0;ide2";                                                           //Update boot order
        String bootdisk = data.optString("bootdisk", null);
        String cdrom = data.optString("cdrom", null);
        String cicustom = data.optString("cicustom", null);
        String cipassword = data.optString("cipassword", null);
        String citype = data.optString("citype", null);
        Boolean ciupgrade = data.has("ciupgrade") ? data.getBoolean("ciupgrade") : null;
        String ciuser = data.optString("ciuser", null);
        Integer cores = data.has("cores") ? data.getInt("cores") : null;
        String cpu = data.optString("cpu", null);
        Float cpulimit = data.has("cpulimit") ? (float) data.getDouble("cpulimit") : null;
        Integer cpuunits = data.has("cpuunits") ? data.getInt("cpuunits") : null;
        String delete = data.optString("delete", null);
        String description = data.optString("description", null);
        String digest = data.optString("digest", null);
        String efidisk0 = data.optString("efidisk0", null);
        Boolean force = data.has("force") ? data.getBoolean("force") : null;
        Boolean freeze = data.has("freeze") ? data.getBoolean("freeze") : null;
        String hookscript = data.optString("hookscript", null);
        String hotplug = data.optString("hotplug", null);
        String hugepages = data.optString("hugepages", null);
        String ivshmem = data.optString("ivshmem", null);
        Boolean keephugepages = data.has("keephugepages") ? data.getBoolean("keephugepages") : null;
        String keyboard = data.optString("keyboard", null);

        Boolean kvm = null;
        if (data.has("kvm")) {
            Object kvmObj = data.get("kvm");
            if (kvmObj instanceof Boolean) {
                kvm = (Boolean) kvmObj;
            } else if (kvmObj instanceof Integer) {
                kvm = ((Integer) kvmObj) != 0;
            }
        }

        Boolean localtime = data.has("localtime") ? data.getBoolean("localtime") : null;
        String lock_ = data.optString("lock", null);
        String machine = data.optString("machine", null);
        String memory = data.optString("memory", null);
        Float migrate_downtime = data.has("migrate_downtime") ? (float) data.getDouble("migrate_downtime") : null;
        Integer migrate_speed = data.has("migrate_speed") ? data.getInt("migrate_speed") : null;
        String name = "testName";                                                                   // Updated VM name
        String nameserver = data.optString("nameserver", null);
        Integer numaInteger = data.has("numa") ? data.getInt("numa") : null;
        Boolean numa = (numaInteger != null && numaInteger != 0);
        Boolean onboot = data.has("onboot") ? data.getBoolean("onboot") : null;
        String ostype = data.optString("ostype", null);
        Boolean protection = data.has("protection") ? data.getBoolean("protection") : null;
        Boolean reboot = data.has("reboot") ? data.getBoolean("reboot") : null;
        String revert = data.optString("revert", null);
        String rng0 = data.optString("rng0", null);
        String scsihw = data.optString("scsihw", null);
        String searchdomain = data.optString("searchdomain", null);
        Integer shares = data.has("shares") ? data.getInt("shares") : null;
        Boolean skiplock = data.has("skiplock") ? data.getBoolean("skiplock") : null;
        String smbios1 = data.optString("smbios1", null);
        Integer smp = data.has("smp") ? data.getInt("smp") : null;
        Integer sockets = data.has("sockets") ? data.getInt("sockets") : null;
        String spice_enhancements = data.optString("spice_enhancements", null);
        String sshkeys = data.optString("sshkeys", null);
        String startdate = data.optString("startdate", null);
        String startup = data.optString("startup", null);
        Boolean tablet = data.has("tablet") ? data.getBoolean("tablet") : null;
        String tags = data.optString("tags", null);
        Boolean tdf = data.has("tdf") ? data.getBoolean("tdf") : null;
        Boolean template = data.has("template") ? data.getBoolean("template") : null;
        String tpmstate0 = data.optString("tpmstate0", null);
        Integer vcpus = data.has("vcpus") ? data.getInt("vcpus") : null;
        String vga = data.optString("vga", null);
        String vmgenid = data.optString("vmgenid", null);
        String vmstatestorage = data.optString("vmstatestorage", null);
        String watchdog = data.optString("watchdog", null);

        Map<Integer, String> scsi = new HashMap<>();
        scsi.put(0, "local-lvm:vm-" + vmId + "-disk-0");
        // Call updateVm() with extracted and modified values
        client.getNodes().get(node).getQemu().get(vmId).getConfig().updateVm(
                null,  // Boolean acpi
                null,  // String affinity
                null,  // String agent
                null,  // String amd_sev
                null,  // String arch
                null,  // String args
                null,  // String audio0
                null,  // Boolean autostart
                null,  // Integer balloon
                null,  // String bios
                "order=scsi0;ide2;net0",  // String boot
                null,  // String bootdisk
                null,  // String cdrom
                null,  // String cicustom
                null,  // String cipassword
                null,  // String citype
                null,  // Boolean ciupgrade
                null,  // String ciuser
                null,  // Integer cores
                null,  // String cpu
                null,  // Float cpulimit
                null,  // Integer cpuunits
                null,  // String delete
                null,  // String description
                null,  // String digest
                null,  // String efidisk0
                null,  // Boolean force
                null,  // Boolean freeze
                null,  // String hookscript
                null,  // java.util.Map<Integer, String> hostpciN
                null,  // String hotplug
                null,  // String hugepages
                null,  // java.util.Map<Integer, String> ideN
                null,  // java.util.Map<Integer, String> ipconfigN
                null,  // String ivshmem
                null,  // Boolean keephugepages
                null,  // String keyboard
                false,  // Boolean kvm
                null,  // Boolean localtime
                null,  // String lock_
                null,  // String machine
                null,  // String memory
                null,  // Float migrate_downtime
                null,  // Integer migrate_speed
                "testName",  // String name
                null,  // String nameserver
                networkDevice,  // java.util.Map<Integer, String> netN
                null,  // Boolean numa
                null,  // java.util.Map<Integer, String> numaN
                null,  // Boolean onboot
                null,  // String ostype
                null,  // java.util.Map<Integer, String> parallelN
                null,  // Boolean protection
                null,  // Boolean reboot
                null,  // String revert
                null,  // String rng0
                null,  // java.util.Map<Integer, String> sataN
                scsi,  // java.util.Map<Integer, String> scsiN
                null,  // String scsihw
                null,  // String searchdomain
                null,  // java.util.Map<Integer, String> serialN
                null,  // Integer shares
                null,  // Boolean skiplock
                null,  // String smbios1
                null,  // Integer smp
                null,  // Integer sockets
                null,  // String spice_enhancements
                null,  // String sshkeys
                null,  // String startdate
                null,  // String startup
                null,  // Boolean tablet
                null,  // String tags
                null,  // Boolean tdf
                null,  // Boolean template
                null,  // String tpmstate0
                null,  // java.util.Map<Integer, String> unusedN
                null,  // java.util.Map<Integer, String> usbN
                null,  // Integer vcpus
                null,  // String vga
                null,  // java.util.Map<Integer, String> virtioN
                null,  // String vmgenid
                null,  // String vmstatestorage
                null   // String watchdog
        );
        return true;
    }

    /**
     * Check if a disk is full or not
     *
     * @param diskContent Type of content stored on disk, as described in JSON disk object
     * @return Boolean disk is full
     */
    private boolean checkDiskSpace(String diskContent) {
        String checkContent = "images";
        if (!diskContent.isEmpty()) {
            checkContent = diskContent;
        }

        this.stg = client.getNodes().get(node).getStorage();                        // Refresh storage in case of multiple uploads

        JSONObject obj = stg.index().getResponse();
        JSONArray data = obj.getJSONArray("data");
        List<JSONObject> disksAsJSON = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            if (!data.getJSONObject(i).getString("content").contains(checkContent)) {
                disksAsJSON.add(0, data.getJSONObject(i));
                break;
            }
            disksAsJSON.add(data.getJSONObject(i));
        }

        boolean diskIsFull = true;
        if (disksAsJSON.get(0).getDouble("used_fraction") < 1) {
            diskIsFull = false;
        }

        return diskIsFull;
    }

    /**
     * Check percent use of disk
     *
     * @return Percentage of disk used
     */
    public int getDiskUse() {
        PveClient.PVENodes.PVENodeItem.PVEStorage stg = client.getNodes().get(node).getStorage();   // Get storage object

        JSONObject obj = stg.index().getResponse();                                                 // Get JSON about storage object
        JSONArray data = obj.getJSONArray("data");                                             // Parsing
        List<JSONObject> disksAsJSON = new ArrayList<>();                                           // Save all disks to the list. First disk is the one we're uploading to, at least in theory. Not super sure how to validate this yet
        for (int i = 0; i < data.length(); i++) {
            if (!data.getJSONObject(i).getString("content").contains("images")) {
                disksAsJSON.add(0, data.getJSONObject(i));
                break;
            }
            disksAsJSON.add(data.getJSONObject(i));
        }
        double used_frac = disksAsJSON.get(0).getDouble("used_fraction");
        System.out.println(used_frac);                                                              // sout the info as a temporary solution

        return (int) (used_frac * 100);                                                              // Return disk usage percent
    }

    /**
     * Adds basic networking when lab instance is created (all VMs on same subnet)
     * @param vmInstances Map of vm instances in lab instance (key - mongo ID, value - VM Instance)
     */
    public void addLabInstanceNetworking(Map<String, VmInstance> vmInstances) {
        // Create new simple zone
        String zoneName = createSimpleZone();

        // Create new VNet
        String vNetName = createVNet(zoneName);

        // Create new subnet
        createSubnet(vNetName);

        // Add VMs to subnet
        addVmsToVNet(vNetName, vmInstances);
    }

    /**
     * Creates a Simple Zone in proxmox with next available ID
     * @return String Zone ID of created zone
     */
    private String createSimpleZone() {
        String currentZoneNumber = "aa"; // Start with two-letter name

        // Get all zones
        JSONObject zonesJson = client.getCluster().getSdn().getZones().index().getResponse();
        JSONArray zonesArray = new JSONArray();

        try {
            zonesArray = zonesJson.getJSONArray("data");
        } catch (JSONException e) {
            currentZoneNumber = "aa";
        }

        while (true) {
            boolean matchFound = false;

            for (int i = 0; i < zonesArray.length(); i++) {
                JSONObject zoneObject = zonesArray.getJSONObject(i);
                String zoneName = zoneObject.getString("zone");

                if (Objects.equals(zoneName, currentZoneNumber)) {
                    currentZoneNumber = nextAlpha(currentZoneNumber);
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound) {
                break;
            }
        }

        client.getCluster().getSdn().getZones().create(
                "simple",
                currentZoneNumber,
                null,
                null,
                null,
                null,
                "dnsmasq",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "pve",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Apply changes
        client.getCluster().getSdn().reload();

        return currentZoneNumber;

    }

    /**
     * Finds next string in pattern "aa" -> "ab" ... "az" -> "ba" ... "zz" -> "aaa"
     * @param input Current string in pattern above
     * @return Next string in pattern above
     */
    private static String nextAlpha(String input) {
        StringBuilder result = new StringBuilder();
        int carry = 1;

        for (int i = input.length() - 1; i >= 0; i--) {
            char c = input.charAt(i);
            if (carry == 0) {
                result.insert(0, c);
                continue;
            }

            if (c == 'z') {
                result.insert(0, 'a');
                carry = 1;
            } else {
                result.insert(0, (char) (c + 1));
                carry = 0;
            }
        }

        if (carry == 1) {
            result.insert(0, 'a');
        }

        // Enforce a minimum length of 2 characters
        while (result.length() < 2) {
            result.insert(0, 'a');
        }

        return result.toString();
    }

    /**
     * Creates a Proxmox VNet under the given Zone
     * @param zoneName Name of zone to create VNet under
     * @return Name of new VNet created
     */
    private String createVNet(String zoneName) {

        client.getCluster().getSdn().getVnets().create(zoneName, zoneName);

        // Apply changes
        client.getCluster().getSdn().reload();

        return zoneName;
    }

    /**
     * Creates a new subnet under the given VNet
     * @param vNetName VNet to create subnet under
     */
    private void createSubnet(String vNetName) {
        List<Object> dhcpRanges = new ArrayList<>();
        dhcpRanges.add(DHCP_RANGE);

        // Create basic subnet
        client.getCluster().getSdn().getVnets().get(vNetName).getSubnets().create(
                SUBNET,
                "subnet",
                DNS_SERVER,
                dhcpRanges,
                "local",
                GATEWAY,
                true);

        // Apply changes
        client.getCluster().getSdn().reload();
    }

    /**
     * Adds all VMs in Map to given VNet
     * @param vNetName VNet to add VMs to
     * @param vmInstances Map of VM Instances to add to given VNet
     */
    private void addVmsToVNet(String vNetName, Map<String, VmInstance> vmInstances) {
        List<VmInstance> vmInstanceList = new ArrayList<>(vmInstances.values());

        // Ensure MAC Address are not repeated
        String baseMacAddress = "BC:24:11:E2:98:10";
        String[] parts = baseMacAddress.split(":");
        int lastByte = Integer.parseInt(parts[5], 16);

        // Loop through VMs, edit their network hardware config to add them to new vnet
        for (int i = 0; i < vmInstances.size(); i++) {
            Map<Integer, String> networkDevice = new HashMap<>();
            int newLastByte = (lastByte + i) & 0xFF; // wrap around at 255
            String newMac = String.format("%s:%s:%s:%s:%s:%02X",
                    parts[0], parts[1], parts[2], parts[3], parts[4], newLastByte);

            networkDevice.put(0, NETWORK_DEVICE + "=" + newMac + ",bridge=" + vNetName + ",firewall=1");

            VmInstance vm = vmInstanceList.get(i);
            updateConfig(vm.getProxmoxId(), networkDevice);
        }
    }
}




