package com.can.controllers;
import com.can.pojo.PatientSelfReport;
import com.can.services.PatientSelfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api")
@CrossOrigin
public class ApiPatientSelfReportController {

    @Autowired
    private PatientSelfReportService patientSelfReportService;


    @PostMapping("/secure/patient-self-report/add")
    public ResponseEntity<?> createPatientSelfReport(@RequestBody PatientSelfReport patientSelfReport, Principal principal) {
        try {
            String username = principal.getName();
            PatientSelfReport newReport = patientSelfReportService.addPatientSelfReport(patientSelfReport, username);
            return new ResponseEntity<>(newReport, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/secure/patient-self-report")
    public ResponseEntity<?> updatePatientSelfReport(@RequestBody PatientSelfReport patientSelfReport, Principal principal) {
        try {
            String username = principal.getName();
            PatientSelfReport updatedReport = patientSelfReportService.updatePatientSelfReport(patientSelfReport, username);
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/secure/patient-self-report/{patientId}")
        public ResponseEntity<?> getPatientSelfReport(@PathVariable("patientId") Integer patientId, Principal principal) {
        try {
            String username = principal.getName();
            PatientSelfReport getReport = patientSelfReportService.getPatientSelfReportByPatientId(patientId, username);
            return ResponseEntity.ok(getReport);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}