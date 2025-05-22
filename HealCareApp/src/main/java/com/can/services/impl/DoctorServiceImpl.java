/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.User;
import com.can.repositories.AppointmentRepository;
import com.can.repositories.DoctorRepository;
import com.can.services.DoctorService;
import com.can.services.UserService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;


/**
 *
 * @author Giidavibe
 */
@Service
public class DoctorServiceImpl implements DoctorService {
    @Autowired
    private DoctorRepository docRepo;

    @Autowired
    private AppointmentRepository appRepo;

    @Autowired
    private UserService userService;

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

    // Trong DoctorServiceImpl
    @Override
    public Doctor getCurrentDoctorProfile(String username) {
        // Lấy thông tin người dùng
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Đảm bảo người dùng là bác sĩ
        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Access denied. Only doctors can access this resource");
        }

        // Lấy thông tin bác sĩ
        return this.docRepo.getDoctorById(currentUser.getId());
    }

    @Override
    public Doctor updateDoctorProfile(String username, Doctor doctorData) {
        // Lấy thông tin bác sĩ hiện tại
        User currentUser = userService.getUserByUsername(username);

        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Access denied. Only doctors can access this resource");
        }

        Doctor doctor = this.docRepo.getDoctorById(currentUser.getId());

        // Cập nhật thông tin bác sĩ
        if (doctorData.getSpecialization() != null) {
            doctor.setSpecialization(doctorData.getSpecialization());
        }

        if (doctorData.getHospital() != null) {
            doctor.setHospital(doctorData.getHospital());
        }

        if (doctorData.getBio() != null) {
            doctor.setBio(doctorData.getBio());
        }

        if (doctorData.getExperienceYears() > 0) {
            doctor.setExperienceYears(doctorData.getExperienceYears());
        }

        if (doctorData.getLicenseNumber() != null) {
            doctor.setLicenseNumber(doctorData.getLicenseNumber());
        }

        // Cập nhật thông tin User
        if (doctorData.getUser() != null) {
            User user = doctor.getUser();

            if (doctorData.getUser().getFirstName() != null) {
                user.setFirstName(doctorData.getUser().getFirstName());
            }

            if (doctorData.getUser().getLastName() != null) {
                user.setLastName(doctorData.getUser().getLastName());
            }

            if (doctorData.getUser().getPhoneNumber() != null) {
                user.setPhoneNumber(doctorData.getUser().getPhoneNumber());
            }

            doctor.setUser(user);
        }

        return this.docRepo.updateDoctor(doctor);
    }
}
