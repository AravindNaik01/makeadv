package com.example.demo.service;

import com.example.demo.dao.InfluencerDao;
import com.example.demo.model.Influencer;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Business logic for Influencer profiles, search, and trust-score calculation.
 * Depends only on the {@link InfluencerDao} interface — JPA is hidden behind the DAO.
 */
@Service
public class InfluencerService {

    private final InfluencerDao influencerDao;

    public InfluencerService(InfluencerDao influencerDao) {
        this.influencerDao = influencerDao;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Calculates and sets the trust score, then persists the influencer.
     */
    public String addInfluencer(Influencer influencer) {
        int score = calculateTrustScore(influencer);
        influencer.setTrustScore(score);
        influencerDao.save(influencer);
        return "Saved to DB with score: " + score;
    }

    public List<Influencer> getAllInfluencers() {
        return influencerDao.findAll();
    }

    // ── Search & Ranking ──────────────────────────────────────────────────────

    /**
     * Returns influencers matching both location and category (case-insensitive).
     * Delegates the query entirely to the DAO — no in-memory filtering.
     */
    public List<Influencer> searchInfluencers(String location, String category) {
        return influencerDao.findByLocationIgnoreCaseAndCategoryIgnoreCase(location, category);
    }

    /**
     * Returns all influencers sorted by trust score descending.
     */
    public List<Influencer> getTopInfluencers() {
        return influencerDao.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Influencer::getTrustScore).reversed())
                .toList();
    }

    // ── Trust Score Calculation ───────────────────────────────────────────────

    /**
     * Calculates an influencer's trust score based on engagement, follower/following
     * ratio, and post frequency. Score is bounded between 0 and 90.
     */
    public int calculateTrustScore(Influencer inf) {
        int score = 0;

        int followers = Math.max(inf.getFollowers(), 1); // avoid division by zero
        double engagement = (inf.getLikes() + inf.getComments()) * 100.0 / followers;

        if (engagement > 5)      score += 30;
        else if (engagement > 2) score += 20;
        else                     score += 10;

        score += (inf.getFollowers() > inf.getFollowing()) ? 20 : 10;
        score += (inf.getPosts() > 50) ? 20 : 10;
        score += 20; // baseline quality bonus

        return score;
    }

    public Influencer getMyProfile() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Influencer inf = influencerDao.findByUsernameIgnoreCase(username).orElseGet(() -> {
            Influencer newInf = new Influencer();
            newInf.setUsername(username);
            newInf.setName(username);
            newInf.setTrustScore(calculateTrustScore(newInf));
            return influencerDao.save(newInf);
        });

        if (inf.getName() == null || inf.getName().trim().isEmpty()) {
            inf.setName(username);
            influencerDao.save(inf);
        }
        return inf;
    }

    public Influencer updateMyProfile(Influencer updated) {
        Influencer current = getMyProfile();
        current.setName(updated.getName());
        current.setCategory(updated.getCategory());
        current.setLocation(updated.getLocation());
        current.setFollowers(updated.getFollowers());
        current.setFollowing(updated.getFollowing());
        current.setPosts(updated.getPosts());
        current.setLikes(updated.getLikes());
        current.setComments(updated.getComments());
        current.setTrustScore(calculateTrustScore(current));
        return influencerDao.save(current);
    }
}
