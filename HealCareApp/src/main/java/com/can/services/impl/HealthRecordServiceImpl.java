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
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            System.out.println("Người dùng không tìm thấy: " + username);
            throw new RuntimeException("Người dùng không tìm thấy");
        }

        if (!"ROLE_DOCTOR".equalsIgnoreCase(user.getRole().name())) {
            System.out.println("Chỉ bác sĩ mới có thể tạo kết quả khám");
            try {
                throw new AccessDeniedException("Chỉ bác sĩ mới có thể tạo kết quả khám");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(HealthRecordServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Appointment appointment = appointmentRepository.getAppointmentById(appointmentId);
        if (appointment == null) {
            System.out.println("Lịch hẹn không tìm thấy: " + appointmentId);
            throw new RuntimeException("Lịch hẹn không tìm thấy");
        }

        if (!user.getId().equals(appointment.getDoctor().getId())) {
            System.out.println(" Người dùng với ID " + user.getId() + " không thể tạo kết quả khám cho lịch hẹn " + appointmentId);
            try {
                throw new AccessDeniedException("Bạn không có quyền tạo kết quả khám cho lịch hẹn");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(HealthRecordServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            System.out.println("Lịch hẹn với ID " + appointmentId + " đã được hoàn thành");
            throw new RuntimeException("Lịch hẹn đã được hoàn thành");
        }

        HealthRecord existingRecord = healthRecordRepository.getHealthRecordByAppointmentId(appointmentId);
        if (existingRecord != null) {
            System.out.println("Kết quả khám đã tồn tại cho lịch hẹn: " + appointmentId);
            throw new RuntimeException("Kết quả khám đã tồn tại");
        }

        HealthRecord healthRecord = new HealthRecord();
        healthRecord.setMedicalHistory(medicalHistory);
        healthRecord.setExaminationResults(examinationResults);
        healthRecord.setDiseaseType(diseaseType);
        healthRecord.setAppointment(appointment);
        healthRecord.setCreatedDate(new Date());

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.updateAppointment(appointment);

        HealthRecord savedRecord = healthRecordRepository.createHealthRecord(healthRecord);

        return savedRecord;
    }



}
