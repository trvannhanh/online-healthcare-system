package com.can.services;


import com.can.pojo.HealthRecord;


/**
 *
 * @author DELL
 */
public interface HealthRecordService {
    
    HealthRecord createHealthRecord(Integer appointmentId, String medicalHistory, String examinationResults, String diseaseType, String username);
    
} 