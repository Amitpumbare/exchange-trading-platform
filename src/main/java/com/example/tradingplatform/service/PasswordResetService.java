package com.example.tradingplatform.service;

import com.example.tradingplatform.exception.InvalidResetTokenException;
import com.example.tradingplatform.exception.ResetTokenExpiredException;
import com.example.tradingplatform.exception.UserNotFoundException;
import com.example.tradingplatform.model.PasswordResetToken;
import com.example.tradingplatform.model.User;
import com.example.tradingplatform.repository.PasswordResetTokenRepository;
import com.example.tradingplatform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService=emailService;
    }

    // STEP 1: User requests forgot password
    @Transactional
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

        String resetLink =
                "http://13.233.63.4/reset-password?token=" + token;

        logger.info("🔐 PASSWORD RESET LINK for {}: {}", user.getEmail(), resetLink);

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    // STEP 2: User submits new password
    @Transactional
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