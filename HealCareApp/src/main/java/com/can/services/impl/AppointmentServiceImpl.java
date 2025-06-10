/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.AppointmentRepository;
import com.can.repositories.DoctorRepository;
import com.can.repositories.PatientRepository;
import com.can.repositories.UserRepository;
import com.can.services.AppointmentService;
import com.can.services.EmailService;
import com.can.services.NotificationService;
import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appRepo;
    private JavaMailSender mailSender;
    
    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;
    
    @Autowired
    private UserRepository uServ;
    
    @Autowired
    private DoctorRepository docRepo;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;
    

    @Override
    public Appointment addAppointment(Appointment appointment, String username) {
        
        Patient patient = patientRepo.getPatientById(appointment.getPatient().getId());
        Doctor doctor = doctorRepo.getDoctorById(appointment.getDoctor().getId());
        
        if(patient == null){
            throw new RuntimeException("Bệnh nhân với ID " + appointment.getPatient().getId() + " không tồn tại");
        }
        
        if(doctor == null){
            throw new RuntimeException("Bác sĩ với ID " + appointment.getDoctor().getId() + " không tồn tại");
        }


        if (!patient.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Bạn chỉ có thể đặt lịch hẹn cho chính mình");
        }
        
        if (!doctor.isIsVerified()) {
            throw new IllegalArgumentException("Bác sĩ chưa được xác minh");
        }
        
        if (appointment.getAppointmentDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải trong tương lai");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(appointment.getAppointmentDate());
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        int millis = cal.get(Calendar.MILLISECOND);
        if (minutes % 60 != 0 || seconds != 0 || millis != 0) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải theo khung 60 phút ");
        }

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 8 || (hour >= 17 && minutes > 0) || hour > 17) {
            throw new IllegalArgumentException("Lịch hẹn phải trong khung giờ làm việc (8:00-17:00)");
        }

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setCreatedAt(new Date());

        Appointment savedAppointment = appRepo.addAppointment(appointment);
        
        try {
            emailService.sendAppointmentConfirmationEmail(savedAppointment, patient.getUser());
            notificationService.createAppointmentNotification(savedAppointment.getId(), patient.getUser().getUsername());
        } catch (Exception e) {
            System.out.println("Lỗi gửi mail hoặc gửi thông báo " + savedAppointment.getId() + ": " + e.getMessage());
        }

        return savedAppointment;
    }
    
    @Override
    public Appointment cancelAppointment(int id, String username) {

        Appointment existingAppointment = appRepo.getAppointmentById(id);
        if (existingAppointment == null) {
            throw new RuntimeException("Không tìm thấy lịch hẹn");
        }

        User u = this.uServ.getUserByUsername(username);
        String role = u.getRole().toString().toUpperCase();

        if (!(u.getId() == existingAppointment.getDoctor().getId()) && !(u.getId() == existingAppointment.getPatient().getId()) && !"ADMIN".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền hủy lịch hẹn này");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String status = existingAppointment.getStatus().toString().toUpperCase();

        if ("COMPLETED".equals(status)) {
            throw new IllegalStateException("Chỉ lịch hẹn chưa hoàn thành mới có thể được hủy");
        }

        Date createdAt = existingAppointment.getCreatedAt();
        Date now = new Date();
        Calendar calCreated = Calendar.getInstance();
        Calendar calNow = Calendar.getInstance();
        calCreated.setTime(createdAt);
        calNow.setTime(now);
        long hoursSinceCreation = (calNow.getTimeInMillis() - calCreated.getTimeInMillis()) / (1000 * 60 * 60);

        if (hoursSinceCreation > 24) {
            throw new IllegalStateException("Không thể đổi lịch hẹn sau 24 giờ kể từ khi tạo");
        }

        return this.appRepo.cancelAppointment(id);
    }

    @Override
    public Appointment rescheduleAppointment(int id, Date newDate, String username) {
        Appointment existingAppointment = appRepo.getAppointmentById(id);
        if (existingAppointment == null) {
            throw new RuntimeException("Không thấy lịch hẹn");
        }

        User u = this.uServ.getUserByUsername(username);
        String role = u.getRole().toString().toUpperCase();

        if ("DOCTOR".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền đổi lịch hẹn này1");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if ( !(u.getId() == existingAppointment.getPatient().getId()) && !"ADMIN".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền đổi lịch hẹn này");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String status = existingAppointment.getStatus().toString().toUpperCase();

        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status) ) {
            throw new IllegalStateException("Chỉ lịch hẹn chưa hoàn thành mới có thể được đổi lịch hẹn");
        }

        Date createdAt = existingAppointment.getCreatedAt();
        Date now = new Date();
        Calendar calCreated = Calendar.getInstance();
        Calendar calNow = Calendar.getInstance();
        calCreated.setTime(createdAt);
        calNow.setTime(now);
        long hoursSinceCreation = (calNow.getTimeInMillis() - calCreated.getTimeInMillis()) / (1000 * 60 * 60);

        if (hoursSinceCreation > 24) {
            throw new IllegalStateException("Không thể đổi lịch hẹn sau 24 giờ kể từ khi tạo");
        }

        return this.appRepo.rescheduleAppointment(id, newDate);
    }
    
    
   

    @Override
    public Appointment updateAppointment(Appointment appointment) {
        Appointment updatedAppointment = appRepo.updateAppointment(appointment);
        return updatedAppointment;
    }

    @Override
    public void deleteAppointment(int id) {
        appRepo.deleteAppointment(id);
    }
    
    @Override
    public List<Appointment> getAppointments(Map<String, String> params) throws ParseException {
        return this.appRepo.getAppointments(params);
    }

    @Override
    public List<Appointment> getAllAppointments() throws ParseException {
        return this.appRepo.getAllAppointments();
    }

    @Override
    public Appointment getAppointmentById(int id) {
        return this.appRepo.getAppointmentById(id);
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status, int page) {
        return this.appRepo.getAppointmentsByStatus(status, page);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(int doctorId, int page) {
        return this.appRepo.getAppointmentsByDoctor(doctorId, page);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId, int page) {
        return this.appRepo.getAppointmentsByPatient(patientId, page);
    }

    // Gửi email xác nhận
    private void sendConfirmationEmail(Appointment appointment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(appointment.getPatient().getUser().getEmail());
        message.setSubject("Xác nhận lịch hẹn");
        message.setText(String.format(
                "Chào %s %s,\n\nLịch hẹn của bạn với bác sĩ %s %s đã được xác nhận.\n"
                        + "Thời gian: %s\nVui lòng đến đúng giờ.\n\nTrân trọng,\nHealCareApp",
                appointment.getPatient().getUser().getFirstName(),
                appointment.getPatient().getUser().getLastName(),
                appointment.getDoctor().getUser().getFirstName(),
                appointment.getDoctor().getUser().getLastName(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(appointment.getAppointmentDate())));
        mailSender.send(message);
    }

    

    @Override
    public Appointment confirmAppointment(int id, String username) {

        Appointment existingAppointment = appRepo.getAppointmentById(id);
        if (existingAppointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        User u = this.uServ.getUserByUsername(username);
        String role = u.getRole().toString().toUpperCase();


        if (!(u.getId() == existingAppointment.getPatient().getId()) && !"ADMIN".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền xác nhận lịch hẹn này");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String status = existingAppointment.getStatus().toString().toUpperCase();

        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status) ) {
            throw new IllegalStateException("Chỉ lịch hẹn chờ xác nhận mới có thể được xác nhận");
        }

        return this.appRepo.confirmAppointment(id);
    }
    
    @Override
    public List<Appointment> getAppointmentsWithFilters(Map<String, String> params) {
        return this.appRepo.getAppointmentsWithFilters(params);
    }
    
    @Override
    public List<String> getAvailableSlots(int doctorId, String date) throws ParseException {
        Doctor doctor = docRepo.getDoctorById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID " + doctorId + " not found");
        }
        
        if (!doctor.isIsVerified()) {
        throw new RuntimeException("Bác sĩ chưa được xác nhận, không thể truy cập khung giờ trống.");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date targetDate = dateFormat.parse(date);


        Map<String, String> params = new HashMap<>();
        params.put("doctorId", String.valueOf(doctorId));
        params.put("appointmentDate", date);
        List<Appointment> appointments = appRepo.getAppointments(params);


        List<String> allTimeSlots = new ArrayList<>();
        for (int hour = 8; hour <= 16; hour++) {
            allTimeSlots.add(String.format("%02d:00", hour));
        }

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
