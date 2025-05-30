//package com.can.services.impl;
//
//import com.can.pojo.HealthRecord;
//import com.can.repositories.HealthRecordRepository;
//import com.can.services.HealthRecordService;
//import java.util.List;
//import java.util.Map;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// *
// * @author DELL
// */
//@Service
//public class HealthRecordServiceImpl implements HealthRecordService {
//    @Autowired
//    private HealthRecordRepository healthRecordRepository;
//
//    @Override
//    public List<HealthRecord> getHealthRecords(Map<String, String> params) {
//        return this.healthRecordRepository.getHealthRecords(params);
//    }
//
//    @Override
//    public List<HealthRecord> getAllHealthRecords() {
//        return this.healthRecordRepository.getAllHealthRecords();
//    }
//
//    @Override
//    public HealthRecord getHealthRecordById(int id) {
//        return this.healthRecordRepository.getHealthRecordById(id);
//    }
//
//    @Override
//    public HealthRecord addHealthRecord(HealthRecord healthRecord) {
//        return this.healthRecordRepository.addHealthRecord(healthRecord);
//    }
//
//    @Override
//    public HealthRecord updateHealthRecord(HealthRecord healthRecord) {
//        return this.healthRecordRepository.updateHealthRecord(healthRecord);
//    }
//
//    @Override
//    public void deleteHealthRecord(int id) {
//        this.healthRecordRepository.deleteHealthRecord(id);
//    }
//
//    @Override
//    public List<HealthRecord> getHealthRecordsByPatient(int patientId) {
//        if (patientId <= 0) {
//            throw new IllegalArgumentException("Invalid patient ID");
//        }
//
//        List<HealthRecord> records = healthRecordRepository.getHealthRecordsByPatient(patientId);
//        if (records == null) {
//            throw new RuntimeException("No health records found for patient ID: " + patientId);
//        }
//
//        return records;
//    }
//}
