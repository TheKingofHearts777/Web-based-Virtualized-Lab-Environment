package com.csproj.Cyberlab.API.vmTemplate;

import lombok.Data;

@Data
public class VmTemplateResponseDTO {
    private final String id;
    private final String name;
    private final String description;

    public VmTemplateResponseDTO(VmTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.description = template.getDescription();
    }
}
