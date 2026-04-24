package com.example.demo.dto;

/**
 * Request DTO for user registration and login endpoints.
 * Decouples the HTTP contract from the User JPA entity.
 */
public class AuthRequest {

    private String username;
    private String password;
    /** Optional — defaults to "BUSINESS" if not provided during registration. */
    private String role;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
