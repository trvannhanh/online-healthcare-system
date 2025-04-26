/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.repositories.AppointmentRepository;
import com.can.repositories.DoctorRepository;
import com.can.services.DoctorService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class DoctorServiceImpl implements DoctorService{
    @Autowired
    private DoctorRepository docRepo;
    
    @Autowired
    private AppointmentRepository appRepo;
    
    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        return this.docRepo.getDoctors(params);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return this.docRepo.getAllDoctors();
    }

    @Override
    public Doctor getDoctorById(int id) {
        return this.docRepo.getDoctorById(id);
    }

    @Override
    public Doctor addDoctor(Doctor doctor) {
        return this.docRepo.addDoctor(doctor);
    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        return this.docRepo.updateDoctor(doctor);
    }

    @Override
    public void deleteDoctor(int id) {
        this.docRepo.deleteDoctor(id);
    }

    @Override
    public List<Doctor> getDoctorByVerificationStatus(boolean isVerified, int page) {
        return this.docRepo.getDoctorByVerificationStatus(isVerified, page);
    }

    @Override
    public void verifyDoctor(int doctorId) {
        this.docRepo.verifyDoctor(doctorId);
    }

    @Override
    public boolean isDoctorVerified(int doctorId) {
        return this.docRepo.isDoctorVerified(doctorId);
    }

    @Override
    public List<Doctor> getUnverifiedDoctors(int page) {
        return this.docRepo.getUnverifiedDoctors(page);
    }

    @Override
    public void updateLicenseNumber(int doctorId, String licenseNumber) {
        this.docRepo.updateLicenseNumber(doctorId, licenseNumber);
    }

    @Override
    public List<String> getAvailableTimeSlots(int doctorId, String date) throws ParseException {
        // Kiểm tra bác sĩ tồn tại
        Doctor doctor = docRepo.getDoctorById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID " + doctorId + " not found");
        }

        // Chuyển đổi ngày thành định dạng Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date targetDate = dateFormat.parse(date);

        // Lấy danh sách lịch hẹn của bác sĩ trong ngày đó
        Map<String, String> params = new HashMap<>();
        params.put("doctorId", String.valueOf(doctorId));
        params.put("appointmentDate", date);
        List<Appointment> appointments = appRepo.getAppointments(params);

        // Định nghĩa khung giờ làm việc (8:00 - 17:00, mỗi slot 1 giờ)
        List<String> allTimeSlots = new ArrayList<>();
        for (int hour = 8; hour <= 16; hour++) {
            allTimeSlots.add(String.format("%02d:00", hour));
        }

        // Loại bỏ các khung giờ đã được đặt
        List<String> availableSlots = new ArrayList<>(allTimeSlots);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        for (Appointment appointment : appointments) {
            if (!appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
                String bookedTime = timeFormat.format(appointment.getAppointmentDate());
                availableSlots.remove(bookedTime);
            }
        }

        return availableSlots;
    }
    
}
