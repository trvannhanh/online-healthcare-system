package com.can.services;

import com.can.pojo.Rating;
import com.can.pojo.RatingResponse;

import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
public interface RatingService {
    List<Rating> getAllRatings(Map<String, String> params);
    Rating getRatingById(Integer id);
public List<Rating> getRatingsByDoctorId(Integer doctorId);    
Rating addRating(Rating rating, String username);
    Rating updateRating(Rating rating, String username);
    void deleteRating(Integer ratingId, String username);
    boolean isRatingExist(int ratingId);
    Double getAverageRatingForDoctor(Integer doctorId);
    // Kiểm tra xem bệnh nhân có thể đánh giá cuộc hẹn không
    boolean canPatientRateAppointment(int appointmentId, int patientId);
    //Kiểm tra xem lịch hẹn đã được đánh giá chưa?
    boolean isAppointmentRated(int appointmentId);
        List<RatingResponse> getRatingResponsesByDoctorId(Integer doctorId);
Rating getRatingByAppointmentId(Integer appointmentId); 
} 
