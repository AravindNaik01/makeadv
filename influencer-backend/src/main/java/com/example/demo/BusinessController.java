package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
public class BusinessController {

    private final BusinessService service;

    public BusinessController(BusinessService service) {
        this.service = service;
    }

    @PostMapping("/business")
    public String addBusiness(@RequestBody Business business) {
        return service.addBusiness(business);
    }

    @GetMapping("/businesses")
    public List<Business> getAllBusinesses() {
        return service.getAllBusinesses();
    }
}