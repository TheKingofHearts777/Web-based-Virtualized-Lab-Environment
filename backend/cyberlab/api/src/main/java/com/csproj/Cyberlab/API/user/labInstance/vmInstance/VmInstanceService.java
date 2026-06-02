package com.csproj.Cyberlab.API.user.labInstance.vmInstance;

import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateRepo;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VmInstanceService {

    private final VmTemplateRepo vmTemplateRepo; // need to use repo directly to avoid dependency cycle
    private final VirtualizationProvider virtualizationProvider;

    /**
     * Get a VmInstance by ID
     *
     * @param lab Lab containing the VmInstance
     * @param vmId ID of the requested VmInstance
     * @return found VmInstance
     * @throws NotFoundException No VmInstance associated with supplied ID
     */
    public VmInstance findById(LabInstance lab, String vmId) throws NotFoundException {
        return lab.getVmInstances().values().stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Requested VmInstance not found", null));
    }

    /**
     * Creates a new VM instance from a template.
     * Should only ever be utilized when creating a new LabInstance saving to the persistence layer
     * is handled by LabInstance logic since VmInstance will never exist separate of their parent LabInstance
     *
     * @param vmParentId ID of the parent VM template
     * @return The newly created VM instance
     */
    public VmInstance create(String vmParentId) {
        VmTemplate parent = vmTemplateRepo.findById(vmParentId)
                .orElseThrow(() -> new NotFoundException("Parent VmTemplate not found", null));

        VmInstance vm = virtualizationProvider.createInstance(parent);
        vm.setId(new ObjectId().toString());

        return vm;
    }

    /**
     * Deletes a VmInstance from the upstream VirtualizationProvider
     *
     * @param lab LabInstance containing the VmInstance to delete
     * @param vmId ID of the VmInstance to delete
     * @return Deleted VmInstance
     */
    public VmInstance delete(LabInstance lab, String vmId) {
        VmInstance vm = findById(lab, vmId);

        virtualizationProvider.deleteInstance(vm.getProxmoxId());

        return vm;
    }

    /**
     * Get VNC ticket and port number.
     *
     * @param lab Lab containing the VmInstance
     * @param vmId ID of the VmInstance to connect to
     * @return required VNC credentials
     */
    public VncConnectionResponse getConnString(LabInstance lab, String vmId) {
        VmInstance vmInstance = findById(lab, vmId);

        return virtualizationProvider.getConnectionURI("" + vmInstance.getProxmoxId());
    }
}
