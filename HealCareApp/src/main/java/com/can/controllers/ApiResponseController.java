package com.can.controllers;

import com.can.pojo.Response;
import com.can.pojo.User;
import com.can.pojo.Rating;
import com.can.services.RatingService;
import com.can.services.ResponseService;
import com.can.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api")
public class ApiResponseController {

    @Autowired
    private ResponseService responseService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private RatingService ratingService;

    // Lấy phản hồi theo ID
    @GetMapping("/response/{id}")
    public ResponseEntity<Response> getResponseById(@PathVariable("id") Integer id, Principal principal) {
        
        Response response = responseService.getResponseById(id);
        
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // Thêm một phản hồi mới
    @PostMapping("/secure/response")
    public ResponseEntity<Response> addResponse(@RequestBody Response response, Principal principal) {
        try {
            
            Response newResponse = responseService.addResponse(response, principal.getName());
            return new ResponseEntity<>(newResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cập nhật phản hồi
    @PutMapping("/secure/response/{id}")
    public ResponseEntity<Response> updateResponse(
            @PathVariable("id") Integer id, 
            @RequestBody Response response,
            Principal principal) {
        try {
            
            response.setId(id);
            Response updatedResponse = responseService.updateResponse(response, principal.getName());
            return ResponseEntity.ok(updatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Lấy tất cả các đánh giá về bác sĩ (dùng Principal)
    @GetMapping("/secure/doctor/ratings/{doctorId}")
    public ResponseEntity<List<Rating>> getDoctorRatings(@PathVariable("doctorId") Integer doctorId, Principal principal) {
        try {
            // Lấy thông tin user đang đăng nhập
            User currentUser = userService.getUserByUsername(principal.getName());
            
            // Lấy ID bác sĩ từ người dùng đăng nhập
            doctorId = currentUser.getId();
            
            List<Rating> ratings = ratingService.getRatingsByDoctorId(doctorId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

        // Kiểm tra đánh giá đã được phản hồi hay chưa
    @GetMapping("/response/rating/{ratingId}")
    public ResponseEntity<Boolean> isRatingResponsed(@PathVariable("ratingId") Integer ratingId) {
        try {
            boolean isRated = responseService.isRatingResponsed(ratingId);
            return ResponseEntity.ok(isRated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    
     @GetMapping("/response/by-rating/{ratingId}")
    public ResponseEntity<Response> getRatingByAppointmentId(@PathVariable("ratingId") Integer ratingId) {
        try {
            Response response = responseService.getResponsesByRating(ratingId);
            return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}