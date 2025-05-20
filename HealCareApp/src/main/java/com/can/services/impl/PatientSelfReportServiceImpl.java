package com.can.services.impl;

import com.can.pojo.Patient;
import com.can.pojo.PatientSelfReport;
import com.can.pojo.User;
import com.can.repositories.PatientSelfReportRepository;
import com.can.services.PatientSelfReportService;
import com.can.services.PatientService;
import com.can.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientSelfReportServiceImpl implements PatientSelfReportService {

    @Autowired
    private PatientSelfReportRepository patientSelfReportRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PatientService patientService;

    @Override
    public PatientSelfReport getPatientSelfReportById(int id) {
        return patientSelfReportRepository.getPatientSelfReportById(id);
    }

    @Override
    public PatientSelfReport getPatientSelfReportByPatientId(int patientId) {
        return patientSelfReportRepository.getPatientSelfReportByPatientId(patientId);
    }

    @Override
    public PatientSelfReport getCurrentPatientSelfReport(String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Ensure user is a patient
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can access this resource");
        }

        return patientSelfReportRepository.getPatientSelfReportByPatientId(currentUser.getId());
    }

    @Override
    public PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Ensure user is a patient
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can create self reports");
        }

        // Check if patient already has a self report
        if (patientSelfReportRepository.existsByPatientId(currentUser.getId())) {
            throw new RuntimeException("You already have a self report. Please update it instead.");
        }

        // Set patient for the report
        Patient patient = patientService.getPatientById(currentUser.getId());
        patientSelfReport.setPatient(patient);

        return patientSelfReportRepository.addPatientSelfReport(patientSelfReport);
    }

    @Override
    public PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Ensure user is a patient
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Access denied. Only patients can update their self reports");
        }

        // Get existing report
        PatientSelfReport existingReport = patientSelfReportRepository.getPatientSelfReportByPatientId(currentUser.getId());
        if (existingReport == null) {
            throw new RuntimeException("No self report found for this patient");
        }

        // Ensure patient is updating their own report
        if (existingReport.getPatient().getId() != currentUser.getId()) {
            throw new RuntimeException("You can only update your own self report");
        }

        // Set ID and patient from existing report
        patientSelfReport.setId(existingReport.getId());
        patientSelfReport.setPatient(existingReport.getPatient());

        return patientSelfReportRepository.updatePatientSelfReport(patientSelfReport);
    }

    @Override
    public boolean hasPatientSelfReport(int patientId) {
        return patientSelfReportRepository.existsByPatientId(patientId);
    }

    @Override
    public boolean hasCurrentPatientSelfReport(String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Ensure user is a patient
        if (!currentUser.getRole().name().equals("PATIENT")) {
            return false;
        }

        return patientSelfReportRepository.existsByPatientId(currentUser.getId());
    }
}