package com.csproj.Cyberlab.API.user.labInstance.vmInstance;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "vm_instances")
@Data
public class VmInstance {

    @Id
    private String id = new ObjectId().toString();

    @NonNull
    private int proxmoxId;

    @NonNull
    private String vmNode;

    @NonNull
    private String vmName;

    @NonNull
    private Date vmCloneDate;

    @NonNull
    private String vmParentId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VmInstance)) return false;
        if (o == this) return true;

        return ((VmInstance) o).getId().equals(this.id);
    }
}
