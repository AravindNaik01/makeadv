package com.example.demo.dto;

/**
 * Response DTO returned after a successful login.
 * Carries the JWT token, the authenticated user's role, and username so the
 * frontend can redirect to the correct dashboard and identify the user in chat.
 */
public class AuthResponse {

    private String token;
    private String role;
    private String username;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public AuthResponse(String message) {
        this.message = message;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
