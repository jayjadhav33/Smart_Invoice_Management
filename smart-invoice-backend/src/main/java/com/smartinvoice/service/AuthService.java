package com.smartinvoice.service;

import com.smartinvoice.dto.AuthResponse;
import com.smartinvoice.dto.LoginRequest;
import com.smartinvoice.dto.RegisterRequest;
import com.smartinvoice.entity.User;
import com.smartinvoice.exception.DuplicateResourceException;
import com.smartinvoice.repository.UserRepository;
import com.smartinvoice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Register new user
    public AuthResponse register(RegisterRequest request) {

        // Check if username already exists
        if (userRepository.findByUsername(
                request.getUsername()).isPresent()) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername()
                    + "' is already taken");
        }

        // Determine role
        User.Role role = User.Role.STAFF;
        if (request.getRole() != null
                && request.getRole()
                .equalsIgnoreCase("ADMIN")) {
            role = User.Role.ADMIN;
        }

        // Create and save user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(
                request.getPassword()));
        user.setRole(role);

        userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(
                user.getUsername(), role.name());

        return new AuthResponse(
                token,
                user.getUsername(),
                role.name(),
                "User registered successfully");
    }

    // Login user
    public AuthResponse login(LoginRequest request) {

        // Authenticate using Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Load user from DB
        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow();

        // Generate token
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name(),
                "Login successful");
    }
}