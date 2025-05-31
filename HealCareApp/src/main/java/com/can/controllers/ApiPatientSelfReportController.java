package com.can.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.can.pojo.PatientSelfReport;
import com.can.services.PatientSelfReportService;
import com.can.services.PatientService;
import com.can.services.UserService;
import com.can.pojo.Patient;
import com.can.pojo.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/secure/patient-self-report")
@CrossOrigin
public class ApiPatientSelfReportController {

    @Autowired
    private PatientSelfReportService patientSelfReportService;

    // Thêm mới một báo cáo sức khỏe
    @PostMapping("/add")
    public ResponseEntity<?> createPatientSelfReport(@RequestBody PatientSelfReport patientSelfReport, Principal principal) {
        try {
            String username = principal.getName();
            PatientSelfReport newReport = patientSelfReportService.addPatientSelfReport(patientSelfReport, username);
            return new ResponseEntity<>(newReport, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dữ liệu không hợp lệ");
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể tạo báo cáo sức khỏe");
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi hệ thống khi tạo báo cáo sức khỏe");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cập nhật báo cáo sức khỏe
    @PutMapping
    public ResponseEntity<?> updatePatientSelfReport(@RequestBody PatientSelfReport patientSelfReport, Principal principal) {
        try {
            String username = principal.getName();
            PatientSelfReport updatedReport = patientSelfReportService.updatePatientSelfReport(patientSelfReport, username);
            return ResponseEntity.ok(updatedReport);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dữ liệu không hợp lệ");
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không thể cập nhật báo cáo sức khỏe");
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi hệ thống khi cập nhật báo cáo sức khỏe");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}