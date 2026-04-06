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
    public String provisionOrLoginInfluencerFromInstagram(String instagramUserId, String instagramUsername) {
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
        createInfluencerProfile(handle);
        return JwtUtil.generateToken(handle);
    }

    private void createInfluencerProfile(String username) {
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
        inf.setPosts(0);
        inf.setLikes(0);
        inf.setComments(0);
        inf.setTrustScore(calculateInitialTrust(inf));
        influencerRepo.save(inf);
    }

    private int calculateInitialTrust(Influencer inf) {
        // keep it simple + consistent with your existing scoring logic baseline
        return 50;
    }

    // LOGIN (business / legacy only — influencers use Instagram OAuth)
    public String login(User user) {

        User existing = repo.findByUsername(user.getUsername());

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