package com.example.demo.model;

import jakarta.persistence.*;

/**
 * JPA Entity representing a collaboration request from a Business to an Influencer.
 * Status transitions: PENDING → ACCEPTED | REJECTED
 */
@Entity
@Table(name = "connection_requests")
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String influencerUsername;

    /** Valid values: PENDING, ACCEPTED, REJECTED */
    @Column(nullable = false)
    private String status;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getInfluencerUsername() { return influencerUsername; }
    public void setInfluencerUsername(String influencerUsername) { this.influencerUsername = influencerUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
