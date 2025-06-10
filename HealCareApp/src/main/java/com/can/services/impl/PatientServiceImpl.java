/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import com.can.services.PatientService;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Người dùng không tìm thấy");
        }

        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ Bệnh Nhân mới được truy cập");
        }

        return this.patRepo.getPatientById(currentUser.getId());
    }

    @Override
    public Patient updatePatientProfile(String username, Patient patientData) {

        User currentUser = userService.getUserByUsername(username);

        if (currentUser == null) {
            throw new RuntimeException("Người dùng không tìm thấy");
        }

        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ Bệnh Nhân mới được truy cập");
        }
        Patient patient = this.patRepo.getPatientById(currentUser.getId());
        
        if (patientData.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientData.getDateOfBirth());
        }

        if (patientData.getInsuranceNumber() != null) {
            patient.setInsuranceNumber(patientData.getInsuranceNumber());
        }

        if (patientData.getUser() != null) {
            User existingUser = patient.getUser();

            if (patientData.getUser().getFirstName() != null) {
                existingUser.setFirstName(patientData.getUser().getFirstName());
            }

            if (patientData.getUser().getLastName() != null) {
                existingUser.setLastName(patientData.getUser().getLastName());
            }

            if (patientData.getUser().getPhoneNumber() != null) {
                System.out.println("Cập nhật số điện thoại cho: " + patientData.getUser().getPhoneNumber());
                existingUser.setPhoneNumber(patientData.getUser().getPhoneNumber());
            }

            userService.updateUser(existingUser);
        }
        return patRepo.updatePatient(patient);
    }

}