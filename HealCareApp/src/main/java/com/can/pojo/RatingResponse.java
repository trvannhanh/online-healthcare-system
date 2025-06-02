package com.can.pojo;

import java.io.Serializable;


public class RatingResponse implements Serializable {
    private Rating rating;
    private Response response;
    
    // Constructors
    public RatingResponse() {
    }
    
    public RatingResponse(Rating rating, Response response) {
        this.rating = rating;
        this.response = response;
    }
    
    // Getters và Setters
    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
    
    // Phương thức tiện ích
    public boolean hasResponse() {
        return response != null;
    }
}