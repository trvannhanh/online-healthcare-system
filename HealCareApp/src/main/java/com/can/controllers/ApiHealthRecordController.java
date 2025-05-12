package com.can.controllers;

import java.util.List;
import java.util.Map;

import com.can.pojo.HealthRecord;
import com.can.services.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/health-records")
public class ApiHealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    // Thêm mới một hồ sơ sức khỏe
    @PostMapping
    public ResponseEntity<HealthRecord> createHealthRecord(@RequestBody HealthRecord healthRecord) {
        try {
            HealthRecord newHealthRecord = healthRecordService.addHealthRecord(healthRecord);
            return new ResponseEntity<>(newHealthRecord, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Xóa một hồ sơ sức khỏe theo id
    @DeleteMapping("/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHealthRecord(@PathVariable(value = "recordId") Integer id) {
        try {
            healthRecordService.deleteHealthRecord(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Health record not found");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /// Cập nhật một hồ sơ sức khỏe theo id
    @PutMapping("{recordId}")
    public ResponseEntity<HealthRecord> updateHealthRecord(@PathVariable("recordId") Integer id,
            @RequestBody HealthRecord healthRecord) {
        try {
            healthRecord.setId(id.intValue());
            HealthRecord updatedHealthRecord = healthRecordService.updateHealthRecord(healthRecord);
            return new ResponseEntity<>(updatedHealthRecord, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Lấy hồ sơ sức khỏe theo ID
    @GetMapping("/{recordId}")
    public ResponseEntity<HealthRecord> getHealthRecordById(@PathVariable("recordId") Integer id) {
        HealthRecord healthRecord = healthRecordService.getHealthRecordById(id);
        return healthRecord != null ? ResponseEntity.ok(healthRecord) : ResponseEntity.notFound().build();
    }

    // Lấy hồ sơ sức khỏe theo bệnh nhân
    @GetMapping("patient/{patientId}")
    public ResponseEntity<List<HealthRecord>> getHealthRecordsByPatient(@PathVariable("patientId") Integer patientId) {
        try {
        if (patientId == null || patientId <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        System.out.println("Fetching health records for patientId: " + patientId);
        List<HealthRecord> healthRecords = healthRecordService.getHealthRecordsByPatient(patientId);

        if (healthRecords == null || healthRecords.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(healthRecords);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    }

}
