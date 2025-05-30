/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.pojo.Doctor;
import com.can.services.AppointmentService;
import com.can.services.DoctorService;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Giidavibe
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiDoctorController {

    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private AppointmentService appointmentService;

    // API tìm kiếm bác sĩ
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam Map<String, String> params) {
        try {
            List<Doctor> doctors = doctorService.getDoctors(params);
            if (doctors.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(doctors, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // API lấy khung giờ trống
    @GetMapping("/doctors/{doctorId}/available-slots")
    @CrossOrigin
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable("doctorId") int doctorId,
            @RequestParam("date") String date) {
        try {
            List<String> availableSlots = appointmentService.getAvailableSlots(doctorId, date);
            return new ResponseEntity<>(availableSlots, HttpStatus.OK);
        } catch (ParseException e) {
            return new ResponseEntity<>("Định dạng ngày không hợp lệ", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // API mới: Lấy thông tin chi tiết bác sĩ
    @GetMapping("/doctors/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable("id") int id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(doctor, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // API mới: Xác nhận giấy phép bác sĩ bởi quản trị viên
    @PostMapping("/admin/verify-doctor/{doctorId}")
    public ResponseEntity<String> verifyDoctor(@PathVariable("doctorId") int doctorId) {
        try {
            doctorService.verifyDoctor(doctorId);
            return new ResponseEntity<>("Xác nhận giấy phép thành công!", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Lỗi khi xác nhận giấy phép: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
