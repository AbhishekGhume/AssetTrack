package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.dto.AssetResponseDto;
import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.UserRepo;
import com.abhishek.asset_manager.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset")
@Tag(name = "Asset APIs", description = "see all assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping("/get-all")
    @Operation(summary = "Get information of all assets", description = "Allows everyone to get the asset information like its id, name, quantity, status and its assigned asset manager.")
    public ResponseEntity<List<AssetResponseDto>> getAllAssets() {
        return new ResponseEntity<>(assetService.getAllAssets(), HttpStatus.OK);
    }


}
