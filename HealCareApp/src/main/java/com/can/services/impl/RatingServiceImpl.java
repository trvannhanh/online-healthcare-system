package com.can.services.impl;

import com.can.pojo.Doctor;
import com.can.pojo.Patient;
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

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private com.can.repositories.DoctorRepository doctorRepo;

    @Autowired
    private com.can.repositories.PatientRepository patientRepo;

    // Implement the methods defined in RatingService interface here
    @Override
    public List<Rating> getAllRatings(Map<String, String> params) {
        // Implementation code here
        return this.ratingRepository.getAllRatings(params);
    }

    @Override
    public Rating getRatingById(Integer id) {
        // Implementation code here
        return this.ratingRepository.getRatingById(id);
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        // Implementation code here
        return this.ratingRepository.getRatingsByDoctorId(doctorId);
    }

    @Override
    public Rating addRating(Rating rating) {
        // Implementation code here
        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5.");
        }

        // Kiểm tra doctor có tồn tại không
        Doctor doctor = doctorRepo.getDoctorById(rating.getDoctor().getId());
        if (doctor == null) {
            throw new IllegalArgumentException("Bác sĩ không tồn tại.");
        }

        // Kiểm tra patient có tồn tại không
        Patient patient = patientRepo.getPatientById(rating.getPatient().getId());
        if (patient == null) {
            throw new IllegalArgumentException("Bệnh nhân không tồn tại.");
        }

        rating.setDoctor(doctor);
        rating.setPatient(patient);
        rating.setCreatedDate(new Date());

        return ratingRepository.addRating(rating);
    }

    @Override
    public Rating updateRating(Rating rating) {
        // Implementation code here
        return this.ratingRepository.updateRating(rating);
    }

    @Override
    public void deleteRating(Integer ratingId) {
        // Implementation code here
        this.ratingRepository.deleteRating(ratingId);
    }

    @Override
    public boolean isRatingExist(int ratingId) {
        // Implementation code here
        return this.ratingRepository.isRatingExist(ratingId);
    }

    @Override
    public double getAverageRatingByDoctorId(Integer doctorId) {
        // Implementation code here
        List<Rating> ratings = this.ratingRepository.getRatingsByDoctorId(doctorId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        double totalRating = 0.0;
        for (Rating rating : ratings) {
            totalRating += rating.getRating();
        }
        return totalRating / ratings.size();
    }
}
