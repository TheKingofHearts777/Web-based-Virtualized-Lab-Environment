package com.csproj.Cyberlab.API.labTemplate;

import com.csproj.Cyberlab.API.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

//---------------------------------------------------------------
// Manage all LabTemplate related business logic
//---------------------------------------------------------------
@Service
@RequiredArgsConstructor
@Slf4j
public class LabTemplateService {
    private final LabTemplateRepo labTemplateRepo;
    private final VmTemplateService vmTemplateService;

    /**
     * Query database for a LabTemplate by its ID
     *
     * @param id ID of requested LabTemplate
     * @return LabTemplate found
     * @throws NotFoundException no LabTemplate associated with supplied ID
     */
    public LabTemplate findById(String id) throws NotFoundException {
        return labTemplateRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Requested LabTemplate not found", null));
    }

    /**
     * Create a new LabTemplate
     * Validates all document references
     *
     * @param labTemplate Data for new LabTemplate
     * @return new LabTemplate
     */
    public LabTemplate create(LabTemplate labTemplate) {
        labTemplate.getVmTemplateIds().forEach(vmTemplateService::findById);

        return labTemplateRepo.save(labTemplate);
    }

    /**
     * Get list of lab templates existing in database.
     *
     * @return paginated list of lab templates
     */
    public List<LabTemplate> getLabTemplateList(int limit, int offset) {
        PageRequest pageRequest = PageRequest.of(offset, limit);
        Page<LabTemplate> temp = labTemplateRepo.findAll(pageRequest);

        return temp.getContent();
    }

    /*
     * Delete existing LabTemplate and all related resources.
     *
     * @param id ID of LabTemplate to delete
     * @throws NotFoundException if LabTemplate is not found
     */
    public void delete(String id) {
        Optional<LabTemplate> labTemplateToDelete = labTemplateRepo.findById(id);

        if (labTemplateToDelete.isEmpty()) {
            throw new NotFoundException("LabTemplate not found: " + id, null);
        }

        log.info(String.format("Attempting to delete LabTemplate with (id = %s)", id));

        labTemplateRepo.deleteById(id);

        log.info(String.format("Successfully deleted LabTemplate with (id = %s)", id));
    }
}
