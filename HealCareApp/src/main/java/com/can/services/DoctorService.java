/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import com.can.pojo.Doctor;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */
public interface DoctorService {
    List<Doctor> getDoctors(Map<String, String> params);
    List<Doctor> getAllDoctors();
    Doctor getDoctorById(int id);
    Doctor addDoctor(Doctor doctor);
    Doctor updateDoctor(Doctor doctor);
    void deleteDoctor(int id);
    List<Doctor> getDoctorByVerificationStatus(boolean isVerified, int page);
    void verifyDoctor(int doctorId);
    boolean isDoctorVerified(int doctorId);
    List<Doctor> getUnverifiedDoctors(int page);
    void updateLicenseNumber(int doctorId, String licenseNumber);
    Doctor getCurrentDoctorProfile(String username);
    Doctor updateDoctorProfile(String username, Doctor doctorData);
    // String updateDoctorAvatar(String username, MultipartFile avatar);
    // boolean changePassword(String currentPassword, String newPassword);
}
