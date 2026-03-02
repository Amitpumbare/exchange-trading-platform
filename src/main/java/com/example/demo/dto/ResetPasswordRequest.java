package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ResetPasswordRequest(

        UUID token,

        @NotBlank
        @Size(min = 6, message = "Password must be at least 8 characters")
        String newPassword

) {}