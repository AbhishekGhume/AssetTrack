package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.dto.RequestResponseDto;
import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emp")
@Tag(name = "Employee APIs", description = "request, return, and see requests")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("get-all-req")
    @Operation(summary = "Get all requests that employee had made", description = "Allows an employee to see all requests that he/she has made.")
    public ResponseEntity<List<RequestResponseDto>> getAllRequests() {
        return new ResponseEntity<>(employeeService.getAllRequests(), HttpStatus.OK);
    }

    @PostMapping("/request-asset/{assetId}")
    @Operation(summary = "Make a request for the required asset", description = "Allows an employee to make an request for the asset by providing its asset id.")
    public ResponseEntity<?> requestAsset(@PathVariable String assetId) {
        employeeService.requestAsset(assetId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("return/{requestId}")
    @Operation(summary = "Return an asset", description = "Allows an employee to return an asset by providing its asset id.")
    public ResponseEntity<?> returnAsset(@PathVariable String requestId) {
        employeeService.returnAsset(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/get-all-pending")
    @Operation(summary = "Get all pending requests of the employee", description = "Allows an employee to see all his/her pending requests.")
    public ResponseEntity<List<RequestResponseDto>> getAllPending() {
        return new ResponseEntity<>(employeeService.getAllPending(), HttpStatus.OK);
    }

    @GetMapping("/get-all-approved")
    @Operation(summary = "Get all approved requests of the employee", description = "Allows an employee to see all his/her approved requests.")
    public ResponseEntity<List<RequestResponseDto>> getAllApproved() {
        return new ResponseEntity<>(employeeService.getAllApproved(), HttpStatus.OK);
    }

    @GetMapping("/get-all-rejected")
    @Operation(summary = "Get all rejected requests of the employee", description = "Allows an employee to see all his/her rejected requests.")
    public ResponseEntity<List<RequestResponseDto>> getAllRejected() {
        return new ResponseEntity<>(employeeService.getAllRejected(), HttpStatus.OK);
    }

    @GetMapping("/get-all-returned")
    @Operation(summary = "Get all returned requests of the employee", description = "Allows an employee to see all his/her returned requests.")
    public ResponseEntity<List<RequestResponseDto>> getAllReturned() {
        return new ResponseEntity<>(employeeService.getAllReturned(), HttpStatus.OK);
    }
}
