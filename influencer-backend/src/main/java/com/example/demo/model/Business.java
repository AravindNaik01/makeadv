package com.example.demo.model;

import jakarta.persistence.*;

/**
 * JPA Entity representing a Business profile stored in the database.
 * Previously an in-memory POJO — now persisted with JPA.
 */
@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String industry;
    private String location;

    /** Username of the User account that owns this business profile. */
    @Column(unique = true)
    private String username;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
