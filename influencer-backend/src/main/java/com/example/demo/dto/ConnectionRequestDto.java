package com.example.demo.dto;

/**
 * Request DTO for sending a collaboration request from a Business to an Influencer.
 */
public class ConnectionRequestDto {

    private String influencerUsername;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getInfluencerUsername() { return influencerUsername; }
    public void setInfluencerUsername(String influencerUsername) { this.influencerUsername = influencerUsername; }
}
