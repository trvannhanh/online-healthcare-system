package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.HealthRecord;
import com.can.pojo.User;
import com.can.repositories.AppointmentRepository;
import com.can.repositories.HealthRecordRepository;
import com.can.repositories.UserRepository;
import com.can.services.HealthRecordService;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class HealthRecordServiceImpl implements HealthRecordService {
    
    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public HealthRecord createHealthRecord(Integer appointmentId, String medicalHistory, String examinationResults, String diseaseType, String username) {
        // Lấy user
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            throw new RuntimeException("User not found");
        }

        // Kiểm tra vai trò
        if (!"ROLE_DOCTOR".equalsIgnoreCase(user.getRole().name())) {
            System.out.println("Access denied: Only doctors can create health records");
            try {
                throw new AccessDeniedException("Only doctors can create health records");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(HealthRecordServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Lấy lịch hẹn
        Appointment appointment = appointmentRepository.getAppointmentById(appointmentId);
        if (appointment == null) {
            System.out.println("Appointment not found for ID: " + appointmentId);
            throw new RuntimeException("Appointment not found");
        }

        // Kiểm tra bác sĩ sở hữu
        if (!user.getId().equals(appointment.getDoctor().getId())) {
            System.out.println("Access denied: User ID " + user.getId() + " cannot create health record for appointment ID " + appointmentId);
            try {
                throw new AccessDeniedException("You do not have permission to create health record for this appointment");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(HealthRecordServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Kiểm tra trạng thái lịch hẹn
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            System.out.println("Appointment ID " + appointmentId + " is already completed");
            throw new RuntimeException("Appointment is already completed");
        }

        // Kiểm tra HealthRecord tồn tại
        HealthRecord existingRecord = healthRecordRepository.getHealthRecordByAppointmentId(appointmentId);
        if (existingRecord != null) {
            System.out.println("Health record already exists for appointment ID: " + appointmentId);
            throw new RuntimeException("Health record already exists");
        }

        // Tạo HealthRecord
        HealthRecord healthRecord = new HealthRecord();
        healthRecord.setMedicalHistory(medicalHistory);
        healthRecord.setExaminationResults(examinationResults);
        healthRecord.setDiseaseType(diseaseType);
        healthRecord.setAppointment(appointment);
        healthRecord.setCreatedDate(new Date());

        // Cập nhật trạng thái lịch hẹn
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.updateAppointment(appointment);

        // Lưu HealthRecord
        HealthRecord savedRecord = healthRecordRepository.createHealthRecord(healthRecord);

        return savedRecord;
    }



}
