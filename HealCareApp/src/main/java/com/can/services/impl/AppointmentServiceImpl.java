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
import com.can.services.UserService;
import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    public Appointment addAppointment(Appointment appointment, String username) {
        // Kiểm tra sự tồn tại của Patient và Doctor
        Patient patient = patientRepo.getPatientById(appointment.getPatient().getId());
        Doctor doctor = doctorRepo.getDoctorById(appointment.getDoctor().getId());
        
        if(patient == null){
            throw new RuntimeException("Bệnh nhân với ID " + appointment.getPatient().getId() + " không tồn tại");
        }
        
        if(doctor == null){
            throw new RuntimeException("Bác sĩ với ID " + appointment.getDoctor().getId() + " không tồn tại");
        }


        // Kiểm tra quyền sở hữu
        if (!patient.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Bạn chỉ có thể đặt lịch hẹn cho chính mình");
        }
        
        // Kiểm tra trạng thái bác sĩ
        if (!doctor.isIsVerified()) {
            throw new IllegalArgumentException("Bác sĩ chưa được xác minh");
        }
        
        // Kiểm tra appointmentDate trong tương lai
        if (appointment.getAppointmentDate().before(new Date())) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải trong tương lai");
        }

        // Kiểm tra slot 30 phút
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointment.getAppointmentDate());
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        int millis = cal.get(Calendar.MILLISECOND);
        if (minutes % 30 != 0 || seconds != 0 || millis != 0) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải theo khung 30 phút (ví dụ: 09:00, 09:30)");
        }

        // Kiểm tra khung giờ làm việc (8:00-17:00)
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 8 || (hour >= 17 && minutes > 0) || hour > 17) {
            throw new IllegalArgumentException("Lịch hẹn phải trong khung giờ làm việc (8:00-17:00)");
        }

        // Gán giá trị mặc định
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setCreatedAt(new Date());

        Appointment savedAppointment = appRepo.addAppointment(appointment);
        
        // Gửi email xác nhận
        try {
            emailService.sendAppointmentConfirmationEmail(savedAppointment, patient.getUser());
        } catch (Exception e) {
            System.out.println("Failed to send confirmation email for appointment ID " + savedAppointment.getId() + ": " + e.getMessage());
            // Không ném lỗi để không làm gián đoạn lưu lịch hẹn
        }

        return savedAppointment;
    }
    
    
   

    @Override
    public Appointment updateAppointment(Appointment appointment) {
        // Kiểm tra lịch hẹn tồn tại
        // Appointment existingAppointment =
        // appRepo.getAppointmentById(appointment.getId());
        // if (existingAppointment.getAppointmentDate() == null) {
        // throw new IllegalArgumentException("Appointment date is missing");
        // }
        // long diffInMillies = existingAppointment.getAppointmentDate().getTime() - new
        // Date().getTime();
        // long diffInHours = diffInMillies / (1000 * 60 * 60);
        // if (diffInHours < 24) {
        // throw new RuntimeException("Cannot update appointment less than 24 hours
        // before the scheduled time");
        // }

        // Cập nhật lịch hẹn
        Appointment updatedAppointment = appRepo.updateAppointment(appointment);

        // // Gửi email thông báo cập nhật
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(updatedAppointment.getPatient().getUser().getEmail());
        // message.setSubject("Cập nhật lịch hẹn");
        // message.setText(String.format(
        // "Chào %s %s,\n\nLịch hẹn của bạn với bác sĩ %s %s đã được cập nhật.\n" +
        // "Thời gian mới: %s\nVui lòng đến đúng giờ.\n\nTrân trọng,\nHealCareApp",
        // updatedAppointment.getPatient().getUser().getFirstName(),
        // updatedAppointment.getPatient().getUser().getLastName(),
        // updatedAppointment.getDoctor().getUser().getFirstName(),
        // updatedAppointment.getDoctor().getUser().getLastName(),
        // new SimpleDateFormat("yyyy-MM-dd
        // HH:mm").format(updatedAppointment.getAppointmentDate())
        // ));
        // mailSender.send(message);
        return updatedAppointment;
    }

    @Override
    public void deleteAppointment(int id) {
        // Appointment appointment = appRepo.getAppointmentById(id);
        // if (appointment == null) {
        // throw new RuntimeException("Appointment not found");
        // }

        // // Kiểm tra thời gian trước 24 giờ
        // long diffInMillies = appointment.getAppointmentDate().getTime() - new
        // Date().getTime();
        // long diffInHours = diffInMillies / (1000 * 60 * 60);
        // if (diffInHours < 24) {
        // throw new RuntimeException("Cannot cancel appointment less than 24 hours
        // before the scheduled time");
        // }
        appRepo.deleteAppointment(id);

        // // Gửi email thông báo hủy
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(appointment.getPatient().getUser().getEmail());
        // message.setSubject("Hủy lịch hẹn");
        // message.setText(String.format(
        // "Chào %s %s,\n\nLịch hẹn của bạn với bác sĩ %s %s vào lúc %s đã được
        // hủy.\n\nTrân trọng,\nHealCareApp",
        // appointment.getPatient().getUser().getFirstName(),
        // appointment.getPatient().getUser().getLastName(),
        // appointment.getDoctor().getUser().getFirstName(),
        // appointment.getDoctor().getUser().getLastName(),
        // new SimpleDateFormat("yyyy-MM-dd
        // HH:mm").format(appointment.getAppointmentDate())
        // ));
        // mailSender.send(message);
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
    public Appointment cancelAppointment(int id, String username) {

        // Tìm lịch hẹn
        Appointment existingAppointment = appRepo.getAppointmentById(id);
        if (existingAppointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        User u = this.uServ.getUserByUsername(username);
        String role = u.getRole().toString().toUpperCase();

        if (!(u.getId() == existingAppointment.getDoctor().getId()) && !(u.getId() == existingAppointment.getPatient().getId()) && !"ADMIN".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền hủy lịch hẹn này2");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String status = existingAppointment.getStatus().toString().toUpperCase();

        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status)) {
            throw new IllegalStateException("Chỉ lịch hẹn chưa hoàn thành mới có thể được hủy");
        }

        // Kiểm tra thời gian tạo lịch hẹn
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
        // Tìm lịch hẹn
        Appointment existingAppointment = appRepo.getAppointmentById(id);
        if (existingAppointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        User u = this.uServ.getUserByUsername(username);
        String role = u.getRole().toString().toUpperCase();

        if ("DOCTOR".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền hủy lịch hẹn này1");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if ( !(u.getId() == existingAppointment.getPatient().getId()) && !"ADMIN".equalsIgnoreCase(role)) {
            try {
                throw new AccessDeniedException("Bạn không có quyền sửa lịch hẹn này");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String status = existingAppointment.getStatus().toString().toUpperCase();

        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status) ) {
            throw new IllegalStateException("Chỉ lịch hẹn chưa hoàn thành mới có thể được xác nhận");
        }

        // Kiểm tra thời gian tạo lịch hẹn
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

        // Gọi repository để reschedule
        return this.appRepo.rescheduleAppointment(id, newDate);
    }

    @Override
    public Appointment confirmAppointment(int id, String username) {

        // Tìm lịch hẹn
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
        // Kiểm tra bác sĩ tồn tại
        Doctor doctor = docRepo.getDoctorById(doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID " + doctorId + " not found");
        }
        
        if (!doctor.isIsVerified()) {
        throw new RuntimeException("Bác sĩ chưa được xác nhận, không thể truy cập khung giờ trống.");
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
