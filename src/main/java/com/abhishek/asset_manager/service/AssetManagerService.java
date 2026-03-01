package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.model.*;
import com.abhishek.asset_manager.repository.AssetRepo;
import com.abhishek.asset_manager.repository.RequestRepo;
import com.abhishek.asset_manager.repository.UserRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AssetManagerService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RequestRepo requestRepo;

    @Autowired
    private AssetRepo assetRepo;

    public List<Request> getAllRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        return user.getRequestList();
    }

    public void approveRequest(ObjectId requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User loggedInManager = userRepo.findByEmail(email);

        // checking if requestId is correct
        Request request = requestRepo.findById(requestId).orElseThrow(() -> new NoSuchElementException("Request with this id is not present"));

        // checking if request status is pending
        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is already " + request.getRequestStatus());
        }

        // finding asset
        Asset asset = assetRepo.findById(request.getAssetId()).orElseThrow(() -> new NoSuchElementException("Asset with this id is not present"));

        // ensuring the correct asset manager for the required asset request
        if (!asset.getAssignedManagerId().equals(loggedInManager.getId())) {
            throw new AccessDeniedException("You are not authorized to approve requests for this asset.");
        }

        if (asset.getQuantity() <= 0) {
            throw new IllegalStateException("Asset is out of stock.");
        }

        // approving and setting approve date
        request.setRequestStatus(RequestStatus.APPROVED);
        request.setApprovedDate(LocalDateTime.now());
        requestRepo.save(request);

        // reducing quantity by 1
        asset.setQuantity(asset.getQuantity()-1);
        if(asset.getQuantity() == 0) asset.setStatus(AssetStatus.UNAVAILABLE);
        assetRepo.save(asset);
    }

    public void rejectRequest(ObjectId requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User loggedInManager = userRepo.findByEmail(email);

        // checking if requestId is correct
        Request request = requestRepo.findById(requestId).orElseThrow(() -> new NoSuchElementException("Request with this id is not present"));

        // finding asset
        Asset asset = assetRepo.findById(request.getAssetId()).orElseThrow(() -> new NoSuchElementException("Asset with this id is not present"));

        // ensuring the correct asset manager for the required asset request
        if (!asset.getAssignedManagerId().equals(loggedInManager.getId())) {
            throw new AccessDeniedException("You are not authorized to reject requests for this asset.");
        }

        // rejecting
        request.setRequestStatus(RequestStatus.REJECTED);
        requestRepo.save(request);
    }

    public List<Request> allPending() {
        List<Request> allRequests = getAllRequests();

        return allRequests.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.PENDING)).toList();
    }

    public List<Request> allApproved() {
        List<Request> allRequests = getAllRequests();

        return allRequests.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.APPROVED)).toList();
    }

    public List<Request> allRejected() {
        List<Request> allRequests = getAllRequests();

        return allRequests.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.REJECTED)).toList();
    }

    public List<Request> allReturned() {
        List<Request> allRequests = getAllRequests();

        return allRequests.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.RETURNED)).toList();
    }
}
