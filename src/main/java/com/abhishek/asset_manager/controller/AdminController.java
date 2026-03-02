package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.model.Asset;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin APIs", description = "create and see assets")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("/create")
    public ResponseEntity<?> createAsset(@RequestBody Asset newAsset) {
        adminService.createAsset(newAsset);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/get-all")
    public ResponseEntity<Map<ObjectId, String>> getAllUsers() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }
}
