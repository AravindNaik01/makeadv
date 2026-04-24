package com.example.demo.controller;

import com.example.demo.model.Business;
import com.example.demo.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MVC Controller — handles HTTP concerns for Business resources.
 * Delegates all business logic to {@link BusinessService}.
 */
@RestController
@CrossOrigin
@RequestMapping("/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /**
     * POST /businesses
     * Adds a new business profile.
     */
    @PostMapping
    public ResponseEntity<String> addBusiness(@RequestBody Business business) {
        return ResponseEntity.ok(businessService.addBusiness(business));
    }

    /**
     * GET /businesses
     * Returns all business profiles.
     */
    @GetMapping
    public ResponseEntity<List<Business>> getAllBusinesses() {
        return ResponseEntity.ok(businessService.getAllBusinesses());
    }

    @GetMapping("/me")
    public ResponseEntity<Business> getMyProfile() {
        return ResponseEntity.ok(businessService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<Business> updateMyProfile(@RequestBody Business business) {
        return ResponseEntity.ok(businessService.updateMyProfile(business));
    }
}
