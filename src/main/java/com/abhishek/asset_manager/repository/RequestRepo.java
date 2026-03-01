package com.abhishek.asset_manager.repository;

import com.abhishek.asset_manager.model.Request;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestRepo extends MongoRepository<Request, ObjectId> {
}
