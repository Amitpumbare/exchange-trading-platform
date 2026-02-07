package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.SignUpRequest;
import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil){
        this.authService = authService;
        this.jwtUtil=jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@Valid @RequestBody SignUpRequest request) {

        User user = authService.createUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        User user = authService.loginAndGetUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

}
