package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.dto.AssetDto;
import com.abhishek.asset_manager.dto.UserResponseDto;
import com.abhishek.asset_manager.model.AssetStatus;
import com.abhishek.asset_manager.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin APIs", description = "create and see users")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create asset with image",
            description = "Allows an Admin to create an asset and assign it to a specific Asset Manager.")
    public ResponseEntity<?> createAsset(
            @RequestPart("name") String name,
            @RequestPart("quantity") String quantity,
            @RequestPart(value = "status", required = false) String status,
            @RequestPart("assignedManagerId") String assignedManagerId,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        AssetDto assetDTO = new AssetDto();
        assetDTO.setName(name);
        assetDTO.setQuantity(Integer.parseInt(quantity));
        assetDTO.setStatus(status != null ? AssetStatus.valueOf(status) : null);
        assetDTO.setAssignedManagerId(assignedManagerId);

        adminService.createAsset(assetDTO, image);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    @Operation(summary = "Get info of all users", description = "Allows an Admin to get the info all all users like their id, name, email and role.")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }
}
