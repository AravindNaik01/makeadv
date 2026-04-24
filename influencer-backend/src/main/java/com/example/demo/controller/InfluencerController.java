package com.example.demo.controller;

import com.example.demo.model.Influencer;
import com.example.demo.service.InfluencerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MVC Controller — handles HTTP concerns for Influencer resources.
 * Delegates all business logic to {@link InfluencerService}.
 */
@RestController
@CrossOrigin
@RequestMapping("/influencers")
public class InfluencerController {

    private final InfluencerService influencerService;

    public InfluencerController(InfluencerService influencerService) {
        this.influencerService = influencerService;
    }

    /**
     * POST /influencers
     * Adds a new influencer profile.
     */
    @PostMapping
    public ResponseEntity<String> addInfluencer(@RequestBody Influencer influencer) {
        return ResponseEntity.ok(influencerService.addInfluencer(influencer));
    }

    /**
     * GET /influencers
     * Returns all influencer profiles.
     */
    @GetMapping
    public ResponseEntity<List<Influencer>> getAllInfluencers() {
        return ResponseEntity.ok(influencerService.getAllInfluencers());
    }

    /**
     * GET /influencers/search?location=...&category=...
     * Filters influencers by location and category.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Influencer>> searchInfluencers(
            @RequestParam String location,
            @RequestParam String category) {
        return ResponseEntity.ok(influencerService.searchInfluencers(location, category));
    }

    /**
     * GET /influencers/top
     * Returns influencers ranked by trust score, highest first.
     */
    @GetMapping("/top")
    public ResponseEntity<List<Influencer>> getTopInfluencers() {
        return ResponseEntity.ok(influencerService.getTopInfluencers());
    }

    @GetMapping("/me")
    public ResponseEntity<Influencer> getMyProfile() {
        return ResponseEntity.ok(influencerService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<Influencer> updateMyProfile(@RequestBody Influencer influencer) {
        return ResponseEntity.ok(influencerService.updateMyProfile(influencer));
    }
}
