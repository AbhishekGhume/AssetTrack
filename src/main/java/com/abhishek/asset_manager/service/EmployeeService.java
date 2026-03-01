package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.exceptions.OutOfStock;
import com.abhishek.asset_manager.exceptions.UserNotExistsException;
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
public class EmployeeService {

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
        return user.getUserRequests();
    }

    public void requestAsset(Request request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User emp = userRepo.findByEmail(auth.getName());

        // Check if user already has a pending request for this specific asset
        boolean alreadyRequested = emp.getUserRequests().stream()
                .anyMatch(req -> req.getAssetId().equals(request.getAssetId())
                        && req.getRequestStatus() == RequestStatus.PENDING);

        if (alreadyRequested) {
            throw new IllegalStateException("You already have a pending request for this asset.");
        }

        ObjectId assetId = request.getAssetId();
        Asset asset = assetRepo.findById(assetId)
                .orElseThrow(() -> new NoSuchElementException("Incorrect asset id"));

        if (asset.getQuantity() == 0) {
            throw new OutOfStock("The requested asset is out of stock.");
        }

        // Check if Manager exists or not
        ObjectId assignedManagerId = asset.getAssignedManagerId();
        User assignedManager = userRepo.findById(assignedManagerId)
                .orElseThrow(() -> new UserNotExistsException("Asset Manager not found"));

        // set request status to pending and request date to now
        request.setRequestStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());

        // Save the request
        // This generates the ID needed for the @DBRef pointers.
        Request savedRequest = requestRepo.save(request);

        // Link the SAVED request to the Manager
        assignedManager.getRequestList().add(savedRequest);
        userRepo.save(assignedManager); // This now has a valid ID to reference

        // Link the SAVED request to the Employee
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User employee = userRepo.findByEmail(authentication.getName());
        employee.getUserRequests().add(savedRequest);
        userRepo.save(employee); // This now has a valid ID to reference
    }

    public void returnAsset(ObjectId requestId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepo.findByEmail(auth.getName());

        // checking if requestId is correct
        Request request = requestRepo.findById(requestId).orElseThrow(() -> new NoSuchElementException("Request with this id is not present"));

        // check if the asset user is returning is belongs to him only i.e. he should not return other asset which does not belongs to him
        if (!currentUser.getUserRequests().contains(request)) {
            throw new AccessDeniedException("You are not authorized to return this asset.");
        }

        // ensure only approved requests are able to return
        if(request.getRequestStatus() != RequestStatus.APPROVED) {
            throw new IllegalStateException("Only approved assets can be returned. Current status: " + request.getRequestStatus());
        }

        // finding asset
        Asset asset = assetRepo.findById(request.getAssetId()).orElseThrow(() -> new NoSuchElementException("Asset with this id is not present"));

        // approving and setting approve date
        request.setRequestStatus(RequestStatus.RETURNED);
        request.setReturnDate(LocalDateTime.now());
        requestRepo.save(request);

        // increasing quantity by 1
        asset.setQuantity(asset.getQuantity()+1);
        if(asset.getStatus().equals(AssetStatus.UNAVAILABLE)) asset.setStatus(AssetStatus.AVAILABLE);
        assetRepo.save(asset);
    }

    public List<Request> getAllPending() {
        List<Request> allReq = getAllRequests();

        return allReq.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.PENDING)).toList();
    }

    public List<Request> getAllApproved() {
        List<Request> allReq = getAllRequests();

        return allReq.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.APPROVED)).toList();
    }

    public List<Request> getAllRejected() {
        List<Request> allReq = getAllRequests();

        return allReq.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.REJECTED)).toList();
    }

    public List<Request> getAllReturned() {
        List<Request> allReq = getAllRequests();

        return allReq.stream()
                .filter(req -> req.getRequestStatus().equals(RequestStatus.RETURNED)).toList();
    }
}
