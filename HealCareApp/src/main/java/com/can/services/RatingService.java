package com.can.services;

import com.can.pojo.Rating;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
public interface RatingService {
    List<Rating> getAllRatings(Map<String, String> params);
    Rating getRatingById(Integer id);
    List<Rating> getRatingsByDoctorId(Integer doctorId);
    Rating addRating(Rating rating);
    Rating updateRating(Rating rating);
    void deleteRating(Integer ratingId);
    boolean isRatingExist(int ratingId);
    Double getAverageRatingForDoctor(Integer doctorId);
} 
