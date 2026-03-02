package com.example.demo.controller;

import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        service.createResetToken(request.email());

        return ResponseEntity.ok().body(
                new MessageResponse("Reset link generated")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        service.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok().body(
                new MessageResponse("Password reset successful")
        );
    }
}