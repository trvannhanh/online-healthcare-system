/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import com.can.pojo.Patient;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Giidavibe
 */
public interface PatientService {
    boolean isUsernameExists(Session session, String username);

    List<Patient> getPatients(Map<String, String> params);

    Patient getPatientById(Integer id);

    Patient addPatient(Patient patient);

    Patient updatePatient(Patient patient);

    void deletePatient(Integer id);

    Patient getCurrentPatientProfile(String username);

    Patient updatePatientProfile(String username, Patient patient);

    boolean changePassword(String currentPassword, String newPassword);

    String updatePatientAvatar(String username, MultipartFile avatar) throws IOException;
}
