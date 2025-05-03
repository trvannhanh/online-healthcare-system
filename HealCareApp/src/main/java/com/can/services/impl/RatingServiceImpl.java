package com.can.services.impl;

import com.can.pojo.Rating;

import com.can.repositories.RatingRepository;
import com.can.services.RatingService;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class RatingServiceImpl implements RatingService {
    // Implement the methods defined in RatingService interface here
    @Override
    public List<Rating> getAllRatings(Map<String, String> params) {
        // Implementation code here
        return null;
    }

    @Override
    public Rating getRatingById(Integer id) {
        // Implementation code here
        return null;
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        // Implementation code here
        return null;
    }

    @Override
    public List<Rating> getRatingsByPatientId(Integer patientId) {
        // Implementation code here
        return null;
    }

    @Override
    public Rating addRating(Rating rating) {
        // Implementation code here
        return null;
    }

    @Override
    public Rating updateRating(Rating rating) {
        // Implementation code here
        return null;
    }

    @Override
    public void deleteRating(Integer ratingId) {
        // Implementation code here
    }

    @Override
    public boolean isRatingExist(int ratingId) {
        // Implementation code here
        return false;
    }
    
}
