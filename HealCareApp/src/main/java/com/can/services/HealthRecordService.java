package com.can.services;

import java.util.List;
import java.util.Map;

import com.can.pojo.HealthRecord;


/**
 *
 * @author DELL
 */
public interface HealthRecordService {
    
    HealthRecord createHealthRecord(Integer appointmentId, String medicalHistory, String examinationResults, String diseaseType, String username);
    
} 