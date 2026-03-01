package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.service.PublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private PublicService publicService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User newUser) {
        publicService.register(newUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        return new ResponseEntity<>(publicService.login(user), HttpStatus.OK);
    }

}
