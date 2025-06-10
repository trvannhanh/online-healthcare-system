package com.can.repositories;

import com.can.pojo.PatientSelfReport;

public interface PatientSelfReportRepository {
    PatientSelfReport getPatientSelfReportById(int id);
    PatientSelfReport getPatientSelfReportByPatientId(int patientId, String username);
    PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport);
    PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport);
    boolean existsByPatientId(int patientId);
}