package com.example.demo.model;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant expiry;

    public PasswordResetToken() {}

    public PasswordResetToken(UUID token, Long userId, Instant expiry) {
        this.token = token;
        this.userId = userId;
        this.expiry = expiry;
    }

    public Long getId() {
        return id;
    }

    public UUID getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }
}
