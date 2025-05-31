package com.can.repositories;

import com.can.pojo.PatientSelfReport;
import java.util.List;

public interface PatientSelfReportRepository {
    PatientSelfReport getPatientSelfReportById(int id);
    PatientSelfReport getPatientSelfReportByPatientId(int patientId, String username);
    PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport);
    PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport);
    boolean existsByPatientId(int patientId);
}