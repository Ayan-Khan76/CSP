package com.example.csp.Model;

public class Review {
    private String text;
    private float rating;
    private String reviewerName;

    public Review(){

    }

    public Review(String text, float rating, String reviewerName) {
        this.text = text;
        this.rating = rating;
        this.reviewerName = reviewerName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

}

