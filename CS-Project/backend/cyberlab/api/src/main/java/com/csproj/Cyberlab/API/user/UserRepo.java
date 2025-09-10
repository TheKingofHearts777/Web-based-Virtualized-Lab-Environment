package com.csproj.Cyberlab.API.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepo extends MongoRepository<User, String> {

    @Query("{ 'username' : ?0 }")
    Optional<User> findByUsername(String username);

    @Query("{ 'labInstances.vmInstances.vmParentId': ?0 }")
    List<User> findByVmInstanceParentId(String parentId);

    @Query("{ 'labInstances.dueDate': { $lte: ?0 } }")
    List<User> findByExpiredLabInstance(Date expirationDate);
}
