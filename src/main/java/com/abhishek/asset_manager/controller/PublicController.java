package com.abhishek.asset_manager.controller;

import com.abhishek.asset_manager.dto.LoginRequestDto;
import com.abhishek.asset_manager.dto.UserRegistrationDto;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.service.PublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
@Tag(name = "Public APIs", description = "register and login")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    @PostMapping("/register")
    @Operation(summary = "Registration for the first time visiting people", description = "Allows users to register by providing name, email, password and role.")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        publicService.register(registrationDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    @Operation(summary = "Login for the users", description = "Allows users to login by providing email, password. It keeps user authenticated for 1 hour.")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return new ResponseEntity<>(publicService.login(loginRequestDto), HttpStatus.OK);
    }

}
