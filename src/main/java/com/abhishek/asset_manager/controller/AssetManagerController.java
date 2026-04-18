package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.dto.RequestResponseDto;
import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.repository.UserRepo;
import com.abhishek.asset_manager.service.AssetManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset-manager")
@Tag(name = "Asset Manager APIs", description = "approve, reject, and see requests")
@RequiredArgsConstructor
public class AssetManagerController {

    private final AssetManagerService assetManagerService;

    @GetMapping("/get-all-req")
    @Operation(summary = "Get all requests came to an asset manager from the employees", description = "Allows an asset manager to see all requests that employees has made.")
    public ResponseEntity<List<RequestResponseDto>> getAllRequests() {
        return new ResponseEntity<>(assetManagerService.getAllRequests(), HttpStatus.OK);
    }

    @GetMapping("/get-all-pending")
    @Operation(summary = "Get all pending requests came to an asset manager from the employees", description = "Allows an asset manager to see all pending requests that employees has made.")
    public ResponseEntity<List<RequestResponseDto>> getAllPending() {
        return new ResponseEntity<>(assetManagerService.allPending(), HttpStatus.OK);
    }

    @GetMapping("/get-all-approved")
    @Operation(summary = "Get all approved requests came to an asset manager from the employees", description = "Allows an asset manager to see all approved requests that employees has made.")
    public ResponseEntity<List<RequestResponseDto>> getAllApproved() {
        return new ResponseEntity<>(assetManagerService.allApproved(), HttpStatus.OK);
    }

    @GetMapping("/get-all-rejected")
    @Operation(summary = "Get all rejected requests came to an asset manager from the employees", description = "Allows an asset manager to see all rejected requests that employees has made.")
    public ResponseEntity<List<RequestResponseDto>> getAllRejected() {
        return new ResponseEntity<>(assetManagerService.allRejected(), HttpStatus.OK);
    }

    @GetMapping("/get-all-returned")
    @Operation(summary = "Get all returned requests came to an asset manager from the employees", description = "Allows an asset manager to see all returned requests that employees has made.")
    public ResponseEntity<List<RequestResponseDto>> getAllReturned() {
        return new ResponseEntity<>(assetManagerService.allReturned(), HttpStatus.OK);
    }

    @PutMapping("/approve/{requestId}")
    @Operation(summary = "Approve the pending request", description = "Allows an asset manager to approve the pending request by providing request id.")
    public ResponseEntity<?> approveRequest(@PathVariable String requestId) {
        assetManagerService.approveRequest(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/reject/{requestId}")
    @Operation(summary = "Reject the pending request", description = "Allows an asset manager to reject the pending request by providing request id.")
    public ResponseEntity<?> rejectRequest(@PathVariable String requestId) {
        assetManagerService.rejectRequest(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
