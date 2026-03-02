package com.example.demo.service;

import com.example.demo.exception.InvalidResetTokenException;
import com.example.demo.exception.ResetTokenExpiredException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // STEP 1: User requests forgot password
    public void createResetToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        // safety: remove old tokens
        tokenRepository.deleteByUserId(user.getId());

        UUID token = UUID.randomUUID();

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        token,
                        user.getId(),
                        Instant.now().plus(15, ChronoUnit.MINUTES)
                );

        tokenRepository.save(resetToken);

        // DEV MODE: print link instead of sending email
        System.out.println(
                "RESET LINK: http://localhost:4200/reset-password?token=" + token
        );
    }

    // STEP 2: User submits new password
    public void resetPassword(UUID token, String newPassword) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidResetTokenException("Invalid reset token"));

        if (resetToken.getExpiry().isBefore(Instant.now())) {
            tokenRepository.delete(resetToken);
            throw new ResetTokenExpiredException("Reset token expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        // delete token after use
        tokenRepository.delete(resetToken);
    }
}