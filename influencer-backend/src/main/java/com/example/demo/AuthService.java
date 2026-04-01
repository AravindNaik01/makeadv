package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private InfluencerRepository influencerRepo;

    // REGISTER
    public String register(User user) {

        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "Username is required";
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return "Password is required";
        }

        String role = user.getRole() == null ? "" : user.getRole().trim().toUpperCase();
        if ("INFLUENCER".equals(role)) {
            return "Influencers must register using Instagram";
        }

        if (repo.findByUsername(user.getUsername()) != null) {
            return "Username already exists";
        }

        repo.save(user);

        return "User registered";
    }

    /**
     * Create or refresh influencer user + profile after Instagram OAuth.
     */
    public String provisionOrLoginInfluencerFromInstagram(String instagramUserId, String instagramUsername, String accountType, int mediaCount, boolean isActive, int recentPosts) {
        if (instagramUserId == null || instagramUserId.isBlank()) {
            return null;
        }
        String handle = instagramUsername == null || instagramUsername.isBlank()
                ? "user_" + instagramUserId
                : instagramUsername.trim();

        var existingByIg = repo.findByInstagramUserId(instagramUserId);
        if (existingByIg.isPresent()) {
            User u = existingByIg.get();
            String oldUsername = u.getUsername();
            if (!handle.equals(oldUsername)) {
                User taken = repo.findByUsername(handle);
                if (taken != null && !taken.getId().equals(u.getId())) {
                    return null;
                }
                u.setUsername(handle);
                repo.save(u);
                final String newHandle = handle;
                influencerRepo.findAll().stream()
                        .filter(i -> oldUsername != null && oldUsername.equalsIgnoreCase(i.getUsername()))
                        .findFirst()
                        .ifPresent(inf -> {
                            inf.setUsername(newHandle);
                            inf.setName(newHandle);
                            influencerRepo.save(inf);
                        });
            }
            
            final String currentHandle = u.getUsername();
            influencerRepo.findAll().stream()
                    .filter(i -> currentHandle.equalsIgnoreCase(i.getUsername()))
                    .findFirst()
                    .ifPresent(inf -> {
                        inf.setPosts(mediaCount);
                        inf.setTrustScore(calculateTrust(accountType, mediaCount, isActive, recentPosts, inf));
                        influencerRepo.save(inf);
                    });

            return JwtUtil.generateToken(u.getUsername());
        }

        if (repo.findByUsername(handle) != null) {
            return null;
        }

        User user = new User();
        user.setInstagramUserId(instagramUserId);
        user.setUsername(handle);
        user.setPassword("OAUTH_IG_" + UUID.randomUUID());
        user.setRole("INFLUENCER");
        repo.save(user);
        createInfluencerProfile(handle, accountType, mediaCount, isActive, recentPosts);
        return JwtUtil.generateToken(handle);
    }

    private void createInfluencerProfile(String username, String accountType, int mediaCount, boolean isActive, int recentPosts) {
        Influencer inf = influencerRepo.findAll().stream()
                .filter(i -> username.equalsIgnoreCase(i.getUsername()))
                .findFirst()
                .orElse(null);
        if (inf != null) {
            return;
        }
        inf = new Influencer();
        inf.setUsername(username);
        inf.setName(username);
        inf.setCategory("General");
        inf.setLocation("Unknown");
        inf.setFollowers(0);
        inf.setFollowing(0);
        inf.setPosts(mediaCount);
        inf.setLikes(0);
        inf.setComments(0);
        inf.setTrustScore(calculateTrust(accountType, mediaCount, isActive, recentPosts, inf));
        influencerRepo.save(inf);
    }

    private int calculateTrust(String accountType, int mediaCount, boolean isActive, int recentPosts, Influencer inf) {
        int score = 50; 
        
        // Boost for account type
        if ("BUSINESS".equalsIgnoreCase(accountType) || "CREATOR".equalsIgnoreCase(accountType)) {
            score += 20;
        }
        
        // Boost for having media
        if (mediaCount > 50) {
            score += 15;
        } else if (mediaCount > 10) {
            score += 10;
        } else if (mediaCount == 0) {
            score -= 20; // potential fake or inactive
        }
        
        // Boost for recent activity
        if (isActive && recentPosts > 0) {
            score += 15;
        } else {
            score -= 10; 
        }

        if (score > 100) score = 100;
        if (score < 0) score = 0;
        
        return score;
    }

    // LOGIN (business only — influencers use Instagram OAuth)
    public String login(User user) {

        User existing = repo.findByUsername(user.getUsername());

        if (existing != null && "INFLUENCER".equalsIgnoreCase(existing.getRole())) {
            return "Influencers must login using Instagram";
        }

        if (existing != null
                && existing.getInstagramUserId() != null
                && !existing.getInstagramUserId().isBlank()) {
            return "Use Instagram to sign in";
        }

        if (existing != null && existing.getPassword().equals(user.getPassword())) {
            return JwtUtil.generateToken(user.getUsername());
        }

        return "Invalid credentials";
    }
}