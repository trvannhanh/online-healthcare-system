package com.can.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.can.pojo.Doctor;
import com.can.services.DoctorService;
import com.can.services.UserService;

@RestController
@RequestMapping("/api/secure/doctor")
@CrossOrigin
public class ApiDoctorProfileController {
    @Autowired
    private DoctorService doctorService;
    
    // Lấy thông tin profile của bác sĩ hiện tại
    @GetMapping("/profile")
    public ResponseEntity<?> getDoctorProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Doctor doctor = doctorService.getCurrentDoctorProfile(username);
            
            // Tạo đối tượng phản hồi
            Map<String, Object> response = new HashMap<>();
            response.put("doctor", doctor);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log lỗi để debug
            e.printStackTrace();

            // Tạo response lỗi chi tiết
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể tải thông tin hồ sơ");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    // Cập nhật thông tin cá nhân
    @PutMapping("/profile")
    public ResponseEntity<?> updateDoctorProfile(@RequestBody Doctor doctor) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Doctor updatedDoctor = doctorService.updateDoctorProfile(username, doctor);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể cập nhật thông tin hồ sơ");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}