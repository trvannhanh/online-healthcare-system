/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.repositories.AppointmentRepository;
import com.can.services.AppointmentService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public Appointment addAppointment(Appointment appointment) {
        // Thêm lịch hẹn
        Appointment newAppointment = appRepo.addAppointment(appointment);
        // Gửi email xác nhận
//        sendConfirmationEmail(newAppointment);
        return newAppointment;
    }

    @Override
    public Appointment updateAppointment(Appointment appointment) {
        // Kiểm tra lịch hẹn tồn tại
//        Appointment existingAppointment = appRepo.getAppointmentById(appointment.getId());
//        if (existingAppointment.getAppointmentDate() == null) {
//            throw new IllegalArgumentException("Appointment date is missing");
//        }
//        long diffInMillies = existingAppointment.getAppointmentDate().getTime() - new Date().getTime();
//        long diffInHours = diffInMillies / (1000 * 60 * 60);
//        if (diffInHours < 24) {
//            throw new RuntimeException("Cannot update appointment less than 24 hours before the scheduled time");
//        }

        // Cập nhật lịch hẹn
        Appointment updatedAppointment = appRepo.updateAppointment(appointment);

//        // Gửi email thông báo cập nhật
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(updatedAppointment.getPatient().getUser().getEmail());
//        message.setSubject("Cập nhật lịch hẹn");
//        message.setText(String.format(
//                "Chào %s %s,\n\nLịch hẹn của bạn với bác sĩ %s %s đã được cập nhật.\n" +
//                "Thời gian mới: %s\nVui lòng đến đúng giờ.\n\nTrân trọng,\nHealCareApp",
//                updatedAppointment.getPatient().getUser().getFirstName(),
//                updatedAppointment.getPatient().getUser().getLastName(),
//                updatedAppointment.getDoctor().getUser().getFirstName(),
//                updatedAppointment.getDoctor().getUser().getLastName(),
//                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(updatedAppointment.getAppointmentDate())
//        ));
//        mailSender.send(message);
        return updatedAppointment;
    }

    @Override
    public void deleteAppointment(int id) {
//        Appointment appointment = appRepo.getAppointmentById(id);
//        if (appointment == null) {
//            throw new RuntimeException("Appointment not found");
//        }

//        // Kiểm tra thời gian trước 24 giờ
//        long diffInMillies = appointment.getAppointmentDate().getTime() - new Date().getTime();
//        long diffInHours = diffInMillies / (1000 * 60 * 60);
//        if (diffInHours < 24) {
//            throw new RuntimeException("Cannot cancel appointment less than 24 hours before the scheduled time");
//        }
        appRepo.deleteAppointment(id);

//        // Gửi email thông báo hủy
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(appointment.getPatient().getUser().getEmail());
//        message.setSubject("Hủy lịch hẹn");
//        message.setText(String.format(
//                "Chào %s %s,\n\nLịch hẹn của bạn với bác sĩ %s %s vào lúc %s đã được hủy.\n\nTrân trọng,\nHealCareApp",
//                appointment.getPatient().getUser().getFirstName(),
//                appointment.getPatient().getUser().getLastName(),
//                appointment.getDoctor().getUser().getFirstName(),
//                appointment.getDoctor().getUser().getLastName(),
//                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(appointment.getAppointmentDate())
//        ));
//        mailSender.send(message);
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
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(appointment.getAppointmentDate())
        ));
        mailSender.send(message);
    }

    @Override
    public Appointment cancelAppointment(int id) {
        return this.appRepo.cancelAppointment(id);
    }

    @Override
    public Appointment rescheduleAppointment(int id, String newDateStr) {
        // Tìm lịch hẹn
        Appointment existingAppointment = appRepo.getAppointmentById(id);
        if (existingAppointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        // Parse ngày mới
        Date newDate;
        try {
            newDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(newDateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd'T'HH:mm:ss");
        }

        return this.appRepo.rescheduleAppointment(id, newDate);
    }

}
