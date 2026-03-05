package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.dto.AssetDto;
import com.abhishek.asset_manager.dto.UserResponseDto;
import com.abhishek.asset_manager.exceptions.UserNotExistsException;
import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.AssetStatus;
import com.abhishek.asset_manager.model.Role;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.AssetRepo;
import com.abhishek.asset_manager.repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AssetRepo assetRepo;

    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream()
                .map(user -> {
                    UserResponseDto userResponseDto = new UserResponseDto();
                    userResponseDto.setId(user.getId().toHexString());
                    userResponseDto.setName(user.getName());
                    userResponseDto.setEmail(user.getEmail());
                    userResponseDto.setRole(user.getRole());
                    return userResponseDto;
                }).toList();
    }

    public void createAsset(AssetDto assetDTO) {
        ObjectId id = new ObjectId(assetDTO.getAssignedManagerId());
        if(userRepo.existsById(id)) {
            Optional<User> optionalUser = userRepo.findById(id);
            if(optionalUser.isPresent()) {
                User user = optionalUser.get();
                if(user.getRole().contains(Role.ASSET_MANAGER)) {
                    Asset asset = new Asset();
                    asset.setName(assetDTO.getName());
                    asset.setQuantity(assetDTO.getQuantity());
                    if(assetDTO.getStatus() == null) asset.setStatus(AssetStatus.AVAILABLE);
                    else asset.setStatus(assetDTO.getStatus());
                    asset.setAssignedManagerId(new ObjectId(assetDTO.getAssignedManagerId()));
                    assetRepo.save(asset);
                }
            }
        } else throw new UserNotExistsException("The asset manager with provided id not present. Check the asset manager id again!");
    }
}
