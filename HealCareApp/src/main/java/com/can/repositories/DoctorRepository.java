/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.repositories;

import com.can.pojo.Doctor;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Giidavibe
 */
public interface DoctorRepository {
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
}
