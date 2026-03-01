package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.UserRepo;
import com.abhishek.asset_manager.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping("/get-all")
    public ResponseEntity<List<Asset>> getAllAssets() {
        return new ResponseEntity<>(assetService.getAllAssets(), HttpStatus.OK);
    }


}
