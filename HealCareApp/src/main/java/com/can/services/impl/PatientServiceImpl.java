/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Patient;
import com.can.repositories.PatientRepository;
import com.can.services.PatientService;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patRepo;

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

}
