package com.csproj.Cyberlab.API.vmTemplate;

import com.csproj.Cyberlab.API.exceptions.FileUploadException;
import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import com.csproj.Cyberlab.API.exceptions.ObjectDeletionException;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.virtualization.VirtualizationProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class VmTemplateService {
    private final VmTemplateRepo vmTemplateRepo;
    private final VirtualizationProvider virtualizationProvider;
    private final UserService userService;

    /**
     * Uploads file from inputStream to Proxmox server, clones base VM, and moves VDI to clone
     * @param inputStream File input stream from HTTP request
     * @param name Name of new VM Template
     * @param description Description of new VM Template
     * @return VmTemplate object with provided values
     * @throws FileUploadException If bad request or server error
     */
    public VmTemplate uploadVdi(InputStream inputStream, String name, String description) throws FileUploadException {
        // Process for posting a VM template will be:
        // 1. validate file / input values
        // 2. upload vdi
        // 3. import vdi to proper logical storage location (local-lvm) (combined with step 2 in implementation below)
        // 4. clone the default VM (id 100)
        // 5. add given vdi instead as storage medium to cloned VM
        // File is validated within uploadFileToSFTP method by analyzing file header hex values
        // Clone the default VM (id 100), wait for task to finish

        VmTemplate template = virtualizationProvider.createTemplate(inputStream, name, description);

        return this.vmTemplateRepo.save(template);
    }


    /**
     * Query database for a VmTemplate by its ID.
     *
     * @param id ID of requested VmTemplate
     * @return VmTemplate if found
     * @throws NotFoundException if VmTemplate not found
     */
    public VmTemplate findById(String id) throws NotFoundException {
        return vmTemplateRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Requested VmTemplate not found", null));
    }

    /**
     * Get list of VM instances existing in database.
     *
     * @return paginated list of VM templates
     */
    public List<VmTemplate> getVmTemplateList(int limit, int offset) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        Page<VmTemplate> temp = vmTemplateRepo.findAll(pageRequest);

        return temp.getContent();
    }

    /*
     * Deletes a VM Template from both Proxmox and MongoDB.
     *
     * @param id ID of the VM Template to delete
     * @throws NotFoundException if the template is not found in MongoDB
     */
    public void delete(String id) {
        log.info(String.format("Attempting to delete vmTemplate with (id = %s)", id));

        List<String> associatedVmInstances = userService.findVmInstancesByParentId(id)
                .stream().map(VmInstance::getId).toList();

        String msgInstances = String.format("Found %d VM Instances linked to VM Template %s", associatedVmInstances.size(), id);

        log.info(msgInstances);

        if (!associatedVmInstances.isEmpty()) {
            throw new ObjectDeletionException(msgInstances);
        }

        VmTemplate template = vmTemplateRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("VM Template not found", null));

        virtualizationProvider.deleteTemplate(template.getProxmoxId());

        vmTemplateRepo.deleteById(id);

        log.info(String.format("Successfully deleted VmTemplate with (id = %s)", id));
    }
}
