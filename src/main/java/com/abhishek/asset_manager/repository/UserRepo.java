package com.abhishek.asset_manager.repository;

import com.abhishek.asset_manager.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, ObjectId> {
    public boolean existsByEmail(String email);

    public User findByEmail(String email);
}
