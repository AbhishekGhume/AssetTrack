package com.abhishek.asset_manager.repository;

import com.abhishek.asset_manager.model.Asset;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssetRepo extends MongoRepository<Asset, ObjectId> {
}
