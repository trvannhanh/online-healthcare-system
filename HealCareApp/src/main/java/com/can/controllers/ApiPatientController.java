/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;
import org.springframework.security.core.Authentication;

import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import com.can.services.UserService;

/**
 *
 * @author Giidavibe
 */
@RestController
@RequestMapping("/api/patient/profile")
public class ApiPatientController {
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserService userService;

    // Get the current patient's profile
    @GetMapping
    public ResponseEntity<?> getPatientProfile() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            // Ensure the user is a patient
            if (!currentUser.getRole().name().equals("PATIENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Only patients can access this resource");
            }
            
            Patient patient = patientRepository.getPatientById(currentUser.getId());
            return ResponseEntity.ok(patient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving profile: " + e.getMessage());
        }
    }
    
    // Update patient profile
    @PutMapping
    public ResponseEntity<?> updatePatientProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String insuranceNumber,
            @RequestParam(required = false) MultipartFile avatar) {
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            
            // Ensure the user is a patient
            if (!currentUser.getRole().name().equals("PATIENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Only patients can access this resource");
            }
            
            Patient patient = patientRepository.getPatientById(currentUser.getId());
            User user = patient.getUser();
            
            // Update User properties if provided
            if (firstName != null && !firstName.isEmpty()) {
                user.setFirstName(firstName);
            }
            
            if (lastName != null && !lastName.isEmpty()) {
                user.setLastName(lastName);
            }
            
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }
            
            if (email != null && !email.isEmpty()) {
                user.setEmail(email);
            }
            
            // Handle avatar upload if provided
            if (avatar != null && !avatar.isEmpty()) {
                // Use the existing avatar upload logic from UserServiceImpl
                Map<String, String> params = new HashMap<>();
                params.put("avatar", "true");
                userService.updateUserAvatar(user.getId(), avatar);
            }
            
            // Update Patient-specific properties
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    Date dob = sdf.parse(dateOfBirth);
                    
                    // Validate date range
                    if (dob.getYear() + 1900 < 1900 || dob.getYear() + 1900 > 9999) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Birth year must be between 1900 and 9999");
                    }
                    
                    patient.setDateOfBirth(dob);
                } catch (ParseException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid date format for dateOfBirth. Expected format: yyyy-MM-dd");
                }
            }
            
            if (insuranceNumber != null && !insuranceNumber.isEmpty()) {
                patient.setInsuranceNumber(insuranceNumber);
            }
            
            // Save changes
            Patient updatedPatient = patientRepository.updatePatient(patient);
            return ResponseEntity.ok(updatedPatient);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile: " + e.getMessage());
        }
    }
    
    // Change password
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Call service to change password
            boolean passwordChanged = userService.changePassword(username, currentPassword, newPassword);
            
            if (passwordChanged) {
                return ResponseEntity.ok("Password changed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password: " + e.getMessage());
        }
    }
}
