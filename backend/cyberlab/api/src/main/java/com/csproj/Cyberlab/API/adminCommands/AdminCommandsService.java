package com.csproj.Cyberlab.API.adminCommands;

import com.csproj.Cyberlab.API.course.Course;
import com.csproj.Cyberlab.API.course.CourseRepo;
import com.csproj.Cyberlab.API.course.CourseService;
import com.csproj.Cyberlab.API.labTemplate.LabTemplate;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateRepo;
import com.csproj.Cyberlab.API.labTemplate.LabTemplateService;
import com.csproj.Cyberlab.API.user.User;
import com.csproj.Cyberlab.API.user.UserRepo;
import com.csproj.Cyberlab.API.user.UserService;
import com.csproj.Cyberlab.API.user.UserType;
import com.csproj.Cyberlab.API.user.labInstance.LabInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstanceService;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateRepo;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AdminCommandsService {
    private final VmInstanceService vmInstanceService;
    private final VmTemplateService vmTemplateService;
    private final VmTemplateRepo vmTemplateRepo;
    private final LabTemplateService labTemplateService;
    private final LabTemplateRepo labTemplateRepo;
    private final UserService userService;
    private final UserRepo userRepo;
    private final CourseService courseService;
    private final CourseRepo courseRepo;

    /**
     * Deletes all non-root VMs, labs, and Non-Admin Users from MongoDB and the Proxmox server
     */
    public void hardReset() {
        List<User> userList = new ArrayList<>(userRepo.findAll());
        List<VmTemplate> vmTemplateList = new ArrayList<>(vmTemplateRepo.findAll());
        List<LabTemplate> labTemplateList = new ArrayList<>(labTemplateRepo.findAll());
        List<Course> courseList = new ArrayList<>(courseRepo.findAll());

        log.info("Starting hard reset: Deleting VM Instances, LabInstances, VM Templates, LabTemplates, and Non-Admin Users.");

        // Deleting all VM Instances
        log.info("Deleting all VM Instances from MongoDB and Proxmox...");
        for (User user : userList) {
            for (LabInstance labInstance : user.getLabInstances().values()) {
                for (String vmId : new ArrayList<>(labInstance.getVmInstances().keySet())) {
                    try {
                        vmInstanceService.delete(labInstance, vmId);
                        log.info(String.format("Deleted VM Instance: %s from LabInstance: %s", vmId, labInstance.getId()));
                    } catch (Exception e) {
                        log.error(String.format("Failed to delete VM Instance: %s from LabInstance: %s", vmId, labInstance.getId()), e);
                    }
                }
            }
        }
        log.info("All VM Instances deleted successfully.");

        // Deleting all VM Templates
        log.info("Deleting all VM Templates from MongoDB and Proxmox...");
        if (!vmTemplateList.isEmpty()) {
            for (VmTemplate vmTemplate : vmTemplateList) {
                if (vmTemplate.getProxmoxId() != 100) {
                    vmTemplateService.delete(vmTemplate.getId());
                    log.info(String.format("Deleted VM Template: %s", vmTemplate.getId()));
                }
            }
        } else {
            log.info("No VM Templates exist.");
        }
        log.info("All VM Templates deleted successfully.");

        // Deleting all LabInstances
        log.info("Deleting all LabInstances from MongoDB...");
        for (User user : userList) {
            for (String labId : new ArrayList<>(user.getLabInstances().keySet())) {
                try {
                    userService.deleteLabInstance(user, user.getLabInstances().get(labId));
                    log.info(String.format("Deleted LabInstance: %s for User: %s", labId, user.getId()));
                } catch (Exception e) {
                    log.error(String.format("Failed to delete LabInstance: %s for User: %s", labId, user.getId()), e);
                }
            }
        }
        log.info("All LabInstances deleted successfully.");

        // Deleting all LabTemplates
        log.info("Deleting all LabTemplates from MongoDB...");
        if (!labTemplateList.isEmpty()) {
            for (LabTemplate labTemplate : labTemplateList) {
                labTemplateService.delete(labTemplate.getId());
                log.info(String.format("Deleted LabTemplate: %s", labTemplate.getId()));
            }
        } else {
            log.info("No LabTemplates exist.");
        }
        log.info("All LabTemplates deleted successfully.");

        // Deleting all Non-Admin Users
        log.info("Deleting all Non-Admin Users from MongoDB...");
        if (!userList.isEmpty()) {
            for (User user : userList) {
                if (user.getUserType() != UserType.Admin) {
                    userService.deleteUser(user.getId());
                    log.info(String.format("Deleted User: %s", user.getId()));
                }
            }
        } else {
            log.info("No Users exist.");
        }
        log.info("All Non-Admin Users deleted successfully.");

        // Deleting all Courses
        log.info("Deleting all courses from MongoDB...");
        if(!courseList.isEmpty()) {
            for (Course course : courseList) {
                courseService.deleteCourse(course.getId());
                log.info(String.format("Deleted Course: %s", course.getId()));
            }
        }
        log.info("All courses deleted successfully.");

        log.info("Hard reset completed successfully.");
    }
}
