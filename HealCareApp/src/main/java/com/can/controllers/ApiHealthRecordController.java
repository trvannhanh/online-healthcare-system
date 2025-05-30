package com.can.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.can.pojo.HealthRecord;
import com.can.services.HealthRecordService;
import com.can.services.PatientService;
import com.can.services.UserService;
import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import com.can.services.PatientService;
import com.can.services.UserService;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/secure")
@CrossOrigin
public class ApiHealthRecordController {
    
    @Autowired
    private HealthRecordService healthRecordService;
    
    @PostMapping("/appointments/{appointmentId}/health-record")
    @PreAuthorize("hasRole('ROLE_DOCTOR')")
    public ResponseEntity<HealthRecord> createHealthRecord(
            @PathVariable("appointmentId") Integer appointmentId,
            @RequestBody Map<String, String> requestBody,
            Principal principal) {
        try {
            String medicalHistory = requestBody.get("medicalHistory");
            String examinationResults = requestBody.get("examinationResults");
            String diseaseType = requestBody.get("diseaseType");

            if (medicalHistory == null || examinationResults == null || diseaseType == null) {
                System.out.println("Thiếu thông tin cần thiết để tạo kết quả khám cho lịch hẹn: " + appointmentId);
                throw new IllegalArgumentException("Thiếu thông tin cần thiết: medicalHistory, examinationResults, diseaseType");
            }

            HealthRecord healthRecord = healthRecordService.createHealthRecord(
                    appointmentId, medicalHistory, examinationResults, diseaseType, principal.getName());
            return ResponseEntity.ok(healthRecord);
        } catch (Exception e) {
            System.out.println("Lỗi tạo kết quả khám cho lịch hẹn: " + appointmentId + ". Error: " + e.getMessage());
            throw e;
        }
    }
}
