package com.example.demo;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BusinessService {

    private List<Business> businessList = new ArrayList<>();

    public String addBusiness(Business business) {
        businessList.add(business);
        return "Business added successfully";
    }

    public List<Business> getAllBusinesses() {
        return businessList;
    }
} 