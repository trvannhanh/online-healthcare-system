package com.can.controllers;

import com.can.pojo.Rating;
import com.can.pojo.Response;
import com.can.services.ResponseService;
import com.can.services.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/rating")
public class ApiRatingController {

    @Autowired
    private RatingService ratingService;
    @Autowired
    private ResponseService responseService;

    // Lấy đánh giá theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable("id") Integer id) {
        Rating rating = ratingService.getRatingById(id);
        return rating != null ? ResponseEntity.ok(rating) : ResponseEntity.notFound().build();
    }

    // Lấy tất cả các đánh giá về bác sĩ theo ID
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Rating>> getRatingsByDoctorId(@PathVariable("doctorId") Integer doctorId) {
        try {
            List<Rating> ratings = ratingService.getRatingsByDoctorId(doctorId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Thêm một đánh giá mới
    @PostMapping
    public ResponseEntity<Rating> addRating(@RequestBody Rating rating) {
        try {
            Rating newRating = ratingService.addRating(rating);
            return new ResponseEntity<>(newRating, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cập nhật đánh giá
    @PutMapping("/{id}")
    public ResponseEntity<Rating> updateRating(@PathVariable Integer id, @RequestBody Rating rating) {
        try {
            if (!ratingService.isRatingExist(id)) {
                return ResponseEntity.notFound().build();
            }
            rating.setId(id);
            Rating updatedRating = ratingService.updateRating(rating);
            return ResponseEntity.ok(updatedRating);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa đánh giá theo ID
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable("id") Integer id) {
        try {
            if (!ratingService.isRatingExist(id)) {
                return ResponseEntity.notFound().build();
            }
            // Lấy và xóa phản hồi liên quan đến đánh giá
            Response response = responseService.getResponsesByRating(id);
            if (response != null) {
                responseService.deleteResponse(response.getId());
            }
            ratingService.deleteRating(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Tính trung bình đánh giá cho nhiều bác sĩ
    @GetMapping("/average/{doctorId}")
    public ResponseEntity<Double> getAverageRatingForDoctor(@PathVariable("doctorId") Integer doctorId) {
        try {
            // Tính trung bình đánh giá cho bác sĩ
            Double averageRating = ratingService.getAverageRatingForDoctor(doctorId);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            System.err.println("Error calculating average rating for doctor ID " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
