package com.csproj.Cyberlab.API.labTemplate;

import org.springframework.data.mongodb.repository.MongoRepository;

//--------------------------------------------
// Interface to enable LabTemplate queries
//--------------------------------------------
public interface LabTemplateRepo extends MongoRepository<LabTemplate, String> {
}
