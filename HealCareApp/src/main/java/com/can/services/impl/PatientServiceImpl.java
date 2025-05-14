/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import com.can.services.PatientService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import com.can.services.UserService;

/**
 *
 * @author Giidavibe
 */
@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private UserService userService;

    @Override
    public boolean isUsernameExists(Session session, String username) {
        return this.patRepo.isUsernameExists(session, username);
    }

    @Override
    public List<Patient> getPatients(Map<String, String> params) {
        return this.patRepo.getPatients(params);
    }

    @Override
    public Patient getPatientById(Integer id) {
        return this.patRepo.getPatientById(id);
    }

    @Override
    public Patient addPatient(Patient patient) {
        return this.patRepo.addPatient(patient);
    }

    @Override
    public Patient updatePatient(Patient patient) {
        return this.patRepo.updatePatient(patient);
    }

    @Override
    public void deletePatient(Integer id) {
        this.patRepo.deletePatient(id);
    }

    @Override
    public Patient getCurrentPatientProfile(String username) {

        // Lấy thông tin người dùng
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Đảm bảo người dùng là bệnh nhân
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can access this resource");
        }

        // Lấy thông tin bệnh nhân
        return this.patRepo.getPatientById(currentUser.getId());
    }

    @Override
    public Patient updatePatientProfile(String username) {
        // Lấy thông tin bệnh nhân hiện tại

        User currentUser = userService.getUserByUsername(username);

        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can access this resource");
        }
        Patient patient = this.patRepo.getPatientById(currentUser.getId());
        // Lưu các thay đổi
        return patRepo.updatePatient(patient);
    }

    @Override
    public boolean changePassword(String currentPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.changePassword(username, currentPassword, newPassword);
    }

    // Implementation in PatientServiceImpl
    @Override
    public String updatePatientAvatar(String username, MultipartFile avatar) throws IOException {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return userService.updateUserAvatar(user.getId(), avatar);
    }
}