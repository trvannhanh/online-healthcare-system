/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;
import org.springframework.security.core.Authentication;

import com.can.pojo.HealthRecord;
import com.can.pojo.Patient;
import com.can.pojo.PatientSelfReport;
import com.can.services.PatientService;
import com.can.services.PatientSelfReportService;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import com.can.services.UserService;

/**
 *
 * @author Giidavibe
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiPatientController {
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private PatientSelfReportService patientSelfReportService;

    @GetMapping("/patients/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable("id") int id) {
        try {
            Patient patient = patientService.getPatientById(id);
            if (patient == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(patient, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    // Get the current patient's profile
    @GetMapping("/secure/patient/profile")
    public ResponseEntity<?> getPatientProfile(Principal principal) {
        try {
            String username = principal.getName();
            System.out.println("Username: " + username);
            Patient patient = patientService.getCurrentPatientProfile(username);
            // Tạo đối tượng phản hồi
            Map<String, Object> response = new HashMap<>();
            response.put("patient", patient);

            // try {
            //     List<HealthRecord> records = patientService.getCurrentPatientHealthRecords(username);
            //     response.put("healthRecords", records);
            // } catch (Exception e) {
            //     // Just log it and continue
            //     System.err.println("Failed to load health records: " + e.getMessage());
            //     response.put("healthRecords", new ArrayList<>());
            // }
            try {
            boolean hasReport = patientSelfReportService.hasCurrentPatientSelfReport(username);
            Map<String, Object> selfReportData = new HashMap<>();
            selfReportData.put("exists", hasReport);
            
            if (hasReport) {
                PatientSelfReport report = patientSelfReportService.getPatientSelfReportByPatientId(patient.getId(), username);
                selfReportData.put("report", report);
            } else {
                selfReportData.put("message", "Bạn chưa có báo cáo sức khỏe. Hãy tạo mới.");
            }
            
            response.put("selfReport", selfReportData);
        } catch (Exception e) {
            System.err.println("Failed to load self report: " + e.getMessage());
            response.put("selfReport", Map.of("exists", false, "message", "Không thể tải thông tin báo cáo sức khỏe"));
        }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log lỗi để debug
            e.printStackTrace();

            // Tạo response lỗi chi tiết hơn
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể tải thông tin hồ sơ");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    // Cập nhật thông tin cá nhân
    @PutMapping("/secure/patient/profile")
    public ResponseEntity<?> updatePatientProfile(
            @RequestBody Patient patient, Principal principal) {

        try {
            Patient updatedPatient = patientService.updatePatientProfile(principal.getName(), patient);
            return ResponseEntity.ok(updatedPatient);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể cập nhật thông tin hồ sơ");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
