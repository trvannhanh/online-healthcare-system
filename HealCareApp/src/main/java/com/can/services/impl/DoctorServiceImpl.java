/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Doctor;
import com.can.repositories.DoctorRepository;
import com.can.services.DoctorService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class DoctorServiceImpl implements DoctorService{
    @Autowired
    private DoctorRepository docRepo;
    
    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        return this.docRepo.getDoctors(params);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return this.docRepo.getAllDoctors();
    }

    @Override
    public Doctor getDoctorById(int id) {
        return this.docRepo.getDoctorById(id);
    }

    @Override
    public Doctor addDoctor(Doctor doctor) {
        return this.docRepo.addDoctor(doctor);
    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        return this.docRepo.updateDoctor(doctor);
    }

    @Override
    public void deleteDoctor(int id) {
        this.docRepo.deleteDoctor(id);
    }

    @Override
    public List<Doctor> getDoctorByVerificationStatus(boolean isVerified, int page) {
        return this.docRepo.getDoctorByVerificationStatus(isVerified, page);
    }

    @Override
    public void verifyDoctor(int doctorId) {
        this.docRepo.verifyDoctor(doctorId);
    }

    @Override
    public boolean isDoctorVerified(int doctorId) {
        return this.docRepo.isDoctorVerified(doctorId);
    }

    @Override
    public List<Doctor> getUnverifiedDoctors(int page) {
        return this.docRepo.getUnverifiedDoctors(page);
    }

    @Override
    public void updateLicenseNumber(int doctorId, String licenseNumber) {
        this.docRepo.updateLicenseNumber(doctorId, licenseNumber);
    }
    
}
