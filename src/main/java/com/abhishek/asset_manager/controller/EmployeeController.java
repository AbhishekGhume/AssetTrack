package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.model.Request;
import com.abhishek.asset_manager.service.EmployeeService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emp")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("get-all-req")
    public ResponseEntity<List<Request>> getAllRequests() {
        return new ResponseEntity<>(employeeService.getAllRequests(), HttpStatus.OK);
    }

    @PostMapping("/request-asset")
    public ResponseEntity<?> requestAsset(@RequestBody Request request) {
        employeeService.requestAsset(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("return/{requestId}")
    public ResponseEntity<?> returnAsset(@PathVariable ObjectId requestId) {
        employeeService.returnAsset(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/get-all-pending")
    public ResponseEntity<List<Request>> getAllPending() {
        return new ResponseEntity<>(employeeService.getAllPending(), HttpStatus.OK);
    }

    @GetMapping("/get-all-approved")
    public ResponseEntity<List<Request>> getAllApproved() {
        return new ResponseEntity<>(employeeService.getAllApproved(), HttpStatus.OK);
    }

    @GetMapping("/get-all-rejected")
    public ResponseEntity<List<Request>> getAllRejected() {
        return new ResponseEntity<>(employeeService.getAllRejected(), HttpStatus.OK);
    }

    @GetMapping("/get-all-returned")
    public ResponseEntity<List<Request>> getAllReturned() {
        return new ResponseEntity<>(employeeService.getAllReturned(), HttpStatus.OK);
    }
}
