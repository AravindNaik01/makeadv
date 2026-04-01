package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class InfluencerService {

	@Autowired
	private InfluencerRepository repo;

	public String addInfluencer(Influencer influencer) {

		int score = calculateScore(influencer);
		influencer.setTrustScore(score);

		repo.save(influencer);

		return "Saved to DB with score: " + score;
	}

	public List<Influencer> getAllInfluencers() {
		return repo.findAll();
	}

	public int calculateScore(Influencer inf) {

		int score = 0;

		int followers = inf.getFollowers();

		// 🔥 FIX: avoid division by zero
		if (followers == 0) followers = 1;

		double engagement = (inf.getLikes() + inf.getComments()) * 100.0 / followers;

		if (engagement > 5) score += 30;
		else if (engagement > 2) score += 20;
		else score += 10;

		if (inf.getFollowers() > inf.getFollowing()) score += 20;
		else score += 10;

		if (inf.getPosts() > 50) score += 20;
		else score += 10;

		score += 20;

		return score;
	}

	public List<Influencer> searchInfluencers(String location, String category) {

	    List<Influencer> result = new ArrayList<>();

	    for (Influencer inf : repo.findAll()) {

	        String infLocation = inf.getLocation();
	        String infCategory = inf.getCategory();

	        if (infLocation != null && infCategory != null &&
	            location != null && category != null &&
	            infLocation.equalsIgnoreCase(location) &&
	            infCategory.equalsIgnoreCase(category)) {

	            result.add(inf);
	        }
	    }

	    return result;
	}

	//RANKING SYSTEM
	public List<Influencer> getTopInfluencers() {

	    List<Influencer> sortedList = new ArrayList<>(repo.findAll());

	    sortedList.sort((a, b) -> b.getTrustScore() - a.getTrustScore());

	    return sortedList;
	}

}