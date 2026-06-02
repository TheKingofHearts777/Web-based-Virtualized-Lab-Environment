package com.csproj.Cyberlab.API.user.labInstance.vmInstance;

import lombok.Data;

import java.util.Date;

@Data
public class VmInstanceResponseDTO {
    private final String id;
    private final String vmName;
    private final Date vmCloneDate;
    private final String vmParentId;

    public VmInstanceResponseDTO(VmInstance vm) {
        this.id = vm.getId();
        this.vmName = vm.getVmName();
        this.vmCloneDate = vm.getVmCloneDate();
        this.vmParentId = vm.getVmParentId();
    }
}
