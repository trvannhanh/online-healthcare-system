/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;
import org.springframework.security.core.Authentication;

import com.can.pojo.Patient;
import com.can.services.PatientService;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import com.can.services.UserService;

/**
 *
 * @author Giidavibe
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiPatientController {
    @Autowired
    private PatientService patientService;
    
    // API mới: Lấy thông tin chi tiết bác sĩ
    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable("id") int id) {
        try {
            Patient patient = patientService.getPatientById(id);
            if (patient == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(patient, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//    // Get the current patient's profile
//    @GetMapping("/profile")
//
//    public ResponseEntity<?> getPatientProfile() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            System.out.println("Username: " + username);
//            Patient patient = patientService.getCurrentPatientProfile(username);
//            return ResponseEntity.ok(patient);
//        } catch (Exception e) {
//            // Log lỗi để debug
//            e.printStackTrace();
//
//            // Tạo response lỗi chi tiết hơn
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Không thể tải thông tin hồ sơ");
//            errorResponse.put("error", e.getMessage());
//            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//            errorResponse.put("timestamp", LocalDateTime.now().toString());
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(errorResponse);
//        }
//    }
//
//    // Cập nhật thông tin cá nhân
//    @PutMapping("/profile")
//    public ResponseEntity<?> updatePatientProfile(
//            @RequestBody Patient patient) {
//
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            Patient updatedPatient = patientService.updatePatientProfile(username,patient);
//            return ResponseEntity.ok(updatedPatient);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Không thể cập nhật thông tin hồ sơ");
//            errorResponse.put("error", e.getMessage());
//            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
//    }
//
//    @PostMapping("/avatar")
//    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile avatar) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//
//            if (avatar == null || avatar.isEmpty()) {
//                return ResponseEntity.badRequest().body("No avatar file provided");
//            }
//
//            // Call service to save the avatar
//            String avatarUrl = patientService.updatePatientAvatar(username, avatar);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Avatar updated successfully");
//            response.put("avatarUrl", avatarUrl);
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error updating avatar: " + e.getMessage());
//        }
//    }
//
//    // Đổi mật khẩu
//    @PostMapping("/change-password")
//    public ResponseEntity<?> changePassword(
//            @RequestParam("currentPassword") String currentPassword,
//            @RequestParam("newPassword") String newPassword) {
//
//        try {
//            boolean changed = patientService.changePassword(currentPassword, newPassword);
//            if (changed) {
//                return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu hiện tại không đúng");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Lỗi khi đổi mật khẩu: " + e.getMessage());
//        }
//    }
    
    
    
}
