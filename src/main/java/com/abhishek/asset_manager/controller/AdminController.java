package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.dto.AssetDto;
import com.abhishek.asset_manager.dto.UserResponseDto;
import com.abhishek.asset_manager.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin APIs", description = "create and see users")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("/create")
    @Operation(summary = "Create a new asset", description = "Allows an Admin to create an asset and assign it to a specific Asset Manager. Validates if the manager exists and has the correct role.")
    public ResponseEntity<?> createAsset(@Valid @RequestBody AssetDto assetDTO) {
        adminService.createAsset(assetDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/get-all")
    @Operation(summary = "Get info of all users", description = "Allows an Admin to get the info all all users like their id, name, email and role.")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }
}
