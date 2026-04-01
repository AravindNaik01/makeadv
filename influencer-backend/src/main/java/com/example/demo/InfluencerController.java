package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
public class InfluencerController {

    private final InfluencerService service;

    public InfluencerController(InfluencerService service) {
        this.service = service;
    }

    @PostMapping("/influencer")
    public String addInfluencer(@RequestBody Influencer influencer) {
        return service.addInfluencer(influencer);
    }

    @GetMapping("/influencers")
    public List<Influencer> getAllInfluencers() {
        return service.getAllInfluencers();
    }
    
    @GetMapping("/search")
    public List<Influencer> searchInfluencers(
            @RequestParam("location") String location,
            @RequestParam("category") String category) {

        return service.searchInfluencers(location, category);
    }
    
    @GetMapping("/top-influencers")
    public List<Influencer> getTopInfluencers() {
        return service.getTopInfluencers();
    }
}