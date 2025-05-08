package com.example.csp.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Freelancer {
    private String id;
    private String name;
    private String profileImageUrl;
    private String about;
    private int hourlyRate;
    private int projectBasedPricing;
    private float averageRating;
    private List<String> skills;
    private List<Review> reviews;
    private Map<String, PortfolioItem> portfolio;

    // Default constructor required for calls to DataSnapshot.getValue(Freelancer.class)
    public Freelancer() {}

    // Getters and setters for all fields

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(int hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getProjectBasedPricing() {
        return projectBasedPricing;
    }

    public void setProjectBasedPricing(int projectBasedPricing) {
        this.projectBasedPricing = projectBasedPricing;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Map<String, PortfolioItem> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Map<String, PortfolioItem> portfolio) {
        this.portfolio = portfolio;
    }

    public List<PortfolioItem> getPortfolioItems() {
        return portfolio != null ? new ArrayList<>(portfolio.values()) : new ArrayList<>();
    }


    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }
}

