package com.csproj.Cyberlab.API.vmTemplate;

import org.springframework.data.mongodb.repository.MongoRepository;

//--------------------------------------------
// Interface to enable VmTemplate queries
//--------------------------------------------
public interface VmTemplateRepo extends MongoRepository<VmTemplate, String> {}
