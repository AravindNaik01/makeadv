package com.example.demo.service;

import com.example.demo.dao.InfluencerDao;
import com.example.demo.dao.UserDao;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.Influencer;
import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import org.springframework.stereotype.Service;

/**
 * Business logic for user registration and login.
 * Supports both INFLUENCER and BUSINESS roles via username/password.
 */
@Service
public class AuthService {

    private final UserDao userDao;
    private final InfluencerDao influencerDao;

    public AuthService(UserDao userDao, InfluencerDao influencerDao) {
        this.userDao = userDao;
        this.influencerDao = influencerDao;
    }

    // ── Registration ─────────────────────────────────────────────────────────

    /**
     * Registers a new user. Auto-provisions an Influencer profile when role is INFLUENCER.
     *
     * @return success message, or an error description on validation failure.
     */
    public String register(AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return "Username is required";
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "Password is required";
        }
        if (userDao.findByUsername(request.getUsername().trim()) != null) {
            return "Username already exists";
        }

        String role = (request.getRole() == null)
                ? "BUSINESS"
                : request.getRole().trim().toUpperCase();

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setRole(role);
        userDao.save(user);

        if ("INFLUENCER".equals(role)) {
            provisionInfluencerProfile(user.getUsername());
        }

        return "User registered";
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a JWT + role response.
     * Works for both INFLUENCER and BUSINESS users.
     */
    public AuthResponse login(AuthRequest request) {
        User existing = userDao.findByUsername(request.getUsername());
        if (existing != null && existing.getPassword().equals(request.getPassword())) {
            String role = (existing.getRole() == null) ? "BUSINESS" : existing.getRole().toUpperCase();

            // Enforce role separation if the request specified a role
            if (request.getRole() != null && !request.getRole().equalsIgnoreCase(role)) {
                return new AuthResponse("Please login via the correct portal. Your account type is " + role + ".");
            }

            String token = JwtUtil.generateToken(existing.getUsername());
            return new AuthResponse(token, role, existing.getUsername());
        }
        return new AuthResponse("Invalid credentials");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void provisionInfluencerProfile(String username) {
        if (influencerDao.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        Influencer inf = new Influencer();
        inf.setUsername(username);
        inf.setName(username);
        inf.setCategory("General");
        inf.setLocation("Unknown");
        inf.setFollowers(0);
        inf.setFollowing(0);
        inf.setPosts(0);
        inf.setLikes(0);
        inf.setComments(0);
        inf.setTrustScore(50);
        influencerDao.save(inf);
    }
}
