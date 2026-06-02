package com.csproj.Cyberlab.API.vmTemplate;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//-------------------------------------------------------
// An entity to represent VmTemplate Mongo Documents
//-------------------------------------------------------

@Document(collection = "Virtual Machines")
@Data
public class VmTemplate {

    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private int proxmoxId;

    @NonNull
    private String proxmoxNode;
}
