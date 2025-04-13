/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.repositories;

import com.can.pojo.Patient;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 *
 * @author Giidavibe
 */
public interface PatientRepository {
    boolean isUsernameExists(Session session, String username);
    List<Patient> getPatients(Map<String, String> params);
    Patient getPatientById(Integer id);
    Patient addPatient(Patient patient);
    Patient updatePatient(Patient patient);
    void deletePatient(Integer id);
}
