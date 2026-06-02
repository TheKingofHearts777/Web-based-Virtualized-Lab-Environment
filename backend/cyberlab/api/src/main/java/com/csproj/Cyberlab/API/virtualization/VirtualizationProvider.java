package com.csproj.Cyberlab.API.virtualization;

import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VmInstance;
import com.csproj.Cyberlab.API.user.labInstance.vmInstance.VncConnectionResponse;
import com.csproj.Cyberlab.API.vmTemplate.VmTemplate;

import java.io.InputStream;
import java.util.Map;

//--------------------------------------------------------------------------------------
// Standardizes interaction and behavior with upstream Virtualization infrastructure
//---------------------------------------------------------------------------------------
public interface VirtualizationProvider {
    //--------------------
    // Instance methods
    //--------------------

    /**
     * Creates a VmInstance by cloning the specified parent VmTemplate
     *
     * @param parentVm VmTemplate to clone from
     * @return newly created VmInstance
     */
    VmInstance createInstance(VmTemplate parentVm);

    void deleteInstance(int id);

    //--------------------
    // Template methods
    //--------------------
    VmTemplate createTemplate(InputStream inputStream, String name, String description);

    void deleteTemplate(int id);

    //--------------------
    // Shared methods
    //--------------------
    VncConnectionResponse getConnectionURI(String id);

    void getById();

    int getDiskUse();

    void addLabInstanceNetworking(Map<String, VmInstance> vmInstances);

}