package com.example.tradingplatform.controller;

import com.example.tradingplatform.dto.ForgotPasswordRequest;
import com.example.tradingplatform.dto.MessageResponse;
import com.example.tradingplatform.dto.ResetPasswordRequest;
import com.example.tradingplatform.service.PasswordResetService;
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