package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.repository.UserRepo;
import com.abhishek.asset_manager.service.AssetManagerService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset-manager")
public class AssetManagerController {

    @Autowired
    private AssetManagerService assetManagerService;

    @GetMapping("/get-all-req")
    public ResponseEntity<List<Request>> getAllRequests() {
        return new ResponseEntity<>(assetManagerService.getAllRequests(), HttpStatus.OK);
    }

    @GetMapping("/get-all-pending")
    public ResponseEntity<List<Request>> getAllPending() {
        return new ResponseEntity<>(assetManagerService.allPending(), HttpStatus.OK);
    }

    @GetMapping("/get-all-approved")
    public ResponseEntity<List<Request>> getAllApproved() {
        return new ResponseEntity<>(assetManagerService.allApproved(), HttpStatus.OK);
    }

    @GetMapping("/get-all-rejected")
    public ResponseEntity<List<Request>> getAllRejected() {
        return new ResponseEntity<>(assetManagerService.allRejected(), HttpStatus.OK);
    }

    @GetMapping("/get-all-returned")
    public ResponseEntity<List<Request>> getAllReturned() {
        return new ResponseEntity<>(assetManagerService.allReturned(), HttpStatus.OK);
    }

    @PutMapping("/approve/{requestId}")
    public ResponseEntity<?> approveRequest(@PathVariable ObjectId requestId) {
        assetManagerService.approveRequest(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable ObjectId requestId) {
        assetManagerService.rejectRequest(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
