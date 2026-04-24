package com.example.demo.service;

import com.example.demo.dao.BusinessDao;
import com.example.demo.model.Business;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for Business profiles.
 * Previously stored in-memory; now backed by the {@link BusinessDao} for proper persistence.
 */
@Service
public class BusinessService {

    private final BusinessDao businessDao;

    public BusinessService(BusinessDao businessDao) {
        this.businessDao = businessDao;
    }

    /**
     * Persists a new Business profile to the database.
     */
    public String addBusiness(Business business) {
        businessDao.save(business);
        return "Business added successfully";
    }

    public List<Business> getAllBusinesses() {
        return businessDao.findAll();
    }

    public Business getMyProfile() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Business b = businessDao.findByUsername(username).orElseGet(() -> {
            Business newBiz = new Business();
            newBiz.setUsername(username);
            newBiz.setName(username);
            return businessDao.save(newBiz);
        });

        if (b.getName() == null || b.getName().trim().isEmpty()) {
            b.setName(username);
            businessDao.save(b);
        }
        return b;
    }

    public Business updateMyProfile(Business updated) {
        Business current = getMyProfile();
        current.setName(updated.getName());
        current.setIndustry(updated.getIndustry());
        current.setLocation(updated.getLocation());
        return businessDao.save(current);
    }
}
