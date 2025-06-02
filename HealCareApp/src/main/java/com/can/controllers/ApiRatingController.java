package com.can.controllers;

import com.can.pojo.Rating;
import com.can.pojo.RatingResponse;
import com.can.services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiRatingController {

    @Autowired
    private RatingService ratingService;
    
    @PostMapping("/secure/patient/rating")
    public ResponseEntity<Rating> addRating(@RequestBody Rating rating, Principal principal) {
        try {
            String username = principal.getName();
            Rating newRating = ratingService.addRating(rating, username);
            return new ResponseEntity<>(newRating, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/rating/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable("id") Integer id) {
        Rating rating = ratingService.getRatingById(id);
        return rating != null ? ResponseEntity.ok(rating) : ResponseEntity.notFound().build();
    }


    @GetMapping("/rating/doctor/{doctorId}")
    public ResponseEntity<List<RatingResponse>> getRatingsByDoctorId(@PathVariable("doctorId") Integer doctorId) {
        try {
            List<RatingResponse> ratings = ratingService.getRatingResponsesByDoctorId(doctorId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/secure/patient/rating/{id}")
    public ResponseEntity<Rating> updateRating(@RequestBody Rating rating, Principal principal) {
        try {
            String username = principal.getName();

            Rating updatedRating = ratingService.updateRating(rating, username);
            return ResponseEntity.ok(updatedRating);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/secure/rating/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable("id") Integer id, Principal principal) {
        try {
            String username = principal.getName();
            ratingService.deleteRating(id, username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/average/{doctorId}")
    public ResponseEntity<Double> getAverageRatingForDoctor(@PathVariable("doctorId") Integer doctorId) {
        try {
            Double averageRating = ratingService.getAverageRatingForDoctor(doctorId);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            System.err.println("Error calculating average rating for doctor ID " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rating/appointment/{appointmentId}")
    public ResponseEntity<Boolean> isAppointmentRated(@PathVariable("appointmentId") Integer appointmentId) {
        try {
            boolean isRated = ratingService.isAppointmentRated(appointmentId);
            return ResponseEntity.ok(isRated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

     @GetMapping("/rating/by-appointment/{appointmentId}")
    public ResponseEntity<Rating> getRatingByAppointmentId(@PathVariable("appointmentId") Integer appointmentId) {
        try {
            Rating rating = ratingService.getRatingByAppointmentId(appointmentId);
            return rating != null ? ResponseEntity.ok(rating) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
