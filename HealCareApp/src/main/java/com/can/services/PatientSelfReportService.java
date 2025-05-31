package com.can.services;

import com.can.pojo.PatientSelfReport;

public interface PatientSelfReportService {
    PatientSelfReport getPatientSelfReportById(int id);
    PatientSelfReport getPatientSelfReportByPatientId(int patientId, String username);
    PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport, String username);
    PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport, String username);
    boolean hasPatientSelfReport(int patientId);
    boolean hasCurrentPatientSelfReport(String username);
}