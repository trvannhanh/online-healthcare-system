/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.pojo.HealthRecord;
import com.can.repositories.PatientRepository;
import com.can.services.PatientService;
import com.can.services.HealthRecordService;

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

    @Autowired
    private HealthRecordService healthRecordService;

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
    public Patient updatePatientProfile(String username, Patient patientData) {
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
        if (patientData.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientData.getDateOfBirth());
        }

        if (patientData.getInsuranceNumber() != null) {
            patient.setInsuranceNumber(patientData.getInsuranceNumber());
        }

        // Quan trọng: Cập nhật thông tin User
        if (patientData.getUser() != null) {
            User existingUser = patient.getUser();

            if (patientData.getUser().getFirstName() != null) {
                existingUser.setFirstName(patientData.getUser().getFirstName());
            }

            if (patientData.getUser().getLastName() != null) {
                existingUser.setLastName(patientData.getUser().getLastName());
            }

            if (patientData.getUser().getPhoneNumber() != null) {
                System.out.println("Updating phone number to: " + patientData.getUser().getPhoneNumber());
                existingUser.setPhoneNumber(patientData.getUser().getPhoneNumber());
            }

            // Lưu thay đổi vào User
            userService.updateUser(existingUser);
        }
        return patRepo.updatePatient(patient);
    }

    @Override
    public List<HealthRecord> getCurrentPatientHealthRecords(String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Đảm bảo người dùng là bệnh nhân
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can access this resource");
        }

        // Lấy thông tin bệnh nhân
        return this.healthRecordService.getHealthRecordsByPatient(currentUser.getId());
    }
}