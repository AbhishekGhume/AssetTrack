package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.dto.AssetDto;
import com.abhishek.asset_manager.dto.UserResponseDto;
import com.abhishek.asset_manager.exceptions.UserAlreadyExistsException;
import com.abhishek.asset_manager.exceptions.UserNotExistsException;
import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.AssetStatus;
import com.abhishek.asset_manager.model.Role;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.AssetRepo;
import com.abhishek.asset_manager.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepo userRepo;
    private final AssetRepo assetRepo;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

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

    public void createAsset(AssetDto assetDTO, MultipartFile image) throws IOException {
        ObjectId id = new ObjectId(assetDTO.getAssignedManagerId());

        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotExistsException(
                        "The asset manager with provided id not present. Check the asset manager id again!"));

        if (!user.getRole().contains(Role.ASSET_MANAGER)) {
            throw new IllegalArgumentException("The provided user is not an Asset Manager.");
        }

        Asset asset = new Asset();
        asset.setName(assetDTO.getName());
        asset.setQuantity(assetDTO.getQuantity());
        asset.setStatus(assetDTO.getStatus() != null ? assetDTO.getStatus() : AssetStatus.AVAILABLE);
        asset.setAssignedManagerId(id);

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(image.getContentType())
                            .build(),
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
            asset.setImageUrl(url);
        }

        assetRepo.save(asset);
    }

    public void makeAdmin(String id) {
        ObjectId objId = new ObjectId(id);
        User user = userRepo.findById(objId).orElseThrow(() -> new UserNotExistsException("User with this id is not present"));
        if(user.getRole().contains(Role.ADMIN)) throw new UserAlreadyExistsException("User is already ADMIN");
        Set<Role> roles = user.getRole();
        roles.add(Role.ADMIN);
        userRepo.save(user);
    }

    public void removeAdmin(String id) {
        ObjectId objId = new ObjectId(id);
        User user = userRepo.findById(objId).orElseThrow(() -> new UserNotExistsException("User with this id is not present"));
        if(!user.getRole().contains(Role.ADMIN)) throw new UserAlreadyExistsException("User is not an ADMIN");
        Set<Role> roles = user.getRole();
        roles.remove(Role.ADMIN);
        userRepo.save(user);
    }
}
