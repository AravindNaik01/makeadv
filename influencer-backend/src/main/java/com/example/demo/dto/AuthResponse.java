package com.example.demo.dto;

/**
 * Response DTO returned after a successful login.
 * Carries the JWT token and the authenticated user's role so the
 * frontend can redirect to the correct dashboard.
 */
public class AuthResponse {

    private String token;
    private String role;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public AuthResponse(String message) {
        this.message = message;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
