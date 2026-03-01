package com.abhishek.asset_manager.service;

import com.abhishek.asset_manager.config.JwtUtil;
import com.abhishek.asset_manager.exceptions.UserAlreadyExistsException;
import com.abhishek.asset_manager.exceptions.UserNotExistsException;
import com.abhishek.asset_manager.model.Role;
import com.abhishek.asset_manager.model.User;
import com.abhishek.asset_manager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PublicService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void register(User user) {
        if(userRepo.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Already registered with this email");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(Set.of(Role.EMPLOYEE)); // default role
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
    }

    public String login(User user) {
        try {

            // here we are checking if user is present or not i.e user is authenticated or not
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            // here we are taking user details
            UserDetails userDetails = userDetailService.loadUserByUsername(user.getEmail());

            return jwtUtil.generateToken(user.getEmail());
        } catch (UserNotExistsException e) {
            throw new UserNotExistsException("Incorrect email or password");
        }
    }
}
