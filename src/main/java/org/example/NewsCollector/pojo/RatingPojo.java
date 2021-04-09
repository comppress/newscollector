package org.example.NewsCollector.pojo;

import org.example.NewsCollector.model.Rating;

public class RatingPojo {

    
    private Rating rating;
    private String userReference;

    public void setUserReference(String userReference) {
        this.userReference = userReference;
    }

    public String getUserReference() {
        return userReference;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }
}
