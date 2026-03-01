package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.exceptions.UserNotExistsException;
import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.Role;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.AssetRepo;
import com.abhishek.asset_manager.repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AssetRepo assetRepo;

    public Map<ObjectId, String> getAllUsers() {
        List<User> users = userRepo.findAll();
        Map<ObjectId, String> map = new HashMap<>();
        if(!users.isEmpty()) {
            for(User user: users) {
                map.put(user.getId(), user.getEmail());
            }
        }
        return map;
    }

    public void createAsset(Asset asset) {
        ObjectId id = asset.getAssignedManagerId();
        if(userRepo.existsById(id)) {
            Optional<User> optionalUser = userRepo.findById(id);
            if(optionalUser.isPresent()) {
                User user = optionalUser.get();
                if(user.getRole().contains(Role.ASSET_MANAGER)) assetRepo.save(asset);
            }
        } else throw new UserNotExistsException("The user with provided id not present. Check the asset manager id again!");
    }
}
