package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.services.EmailService;

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
/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/email")
public class ApiEmailController {
    @Autowired
    private EmailService emailService;

    // Gửi email đơn giản
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {
        try {
            emailService.sendEmail(to, subject, body);
            return new ResponseEntity<>("Email sent successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Gửi email xác nhận lịch hẹn
    @PostMapping("/appointment/confirm")
    public ResponseEntity<String> sendAppointmentConfirmation(@RequestBody Appointment appointment) {
        try {
            emailService.sendAppointmentConfirmationEmail(appointment);
            return new ResponseEntity<>("Appointment confirmation email sent.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send confirmation email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Gửi thông báo nhắc lịch hẹn
    @PostMapping("/appointment/notify")
    public ResponseEntity<String> sendAppointmentNotification(@RequestBody Appointment appointment) {
        try {
            emailService.sendAppointmentNotificationEmail(appointment);
            return new ResponseEntity<>("Appointment notification email sent.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send notification email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Gửi email khuyến mãi đến tất cả bệnh nhân
    @PostMapping("/promotion")
    public ResponseEntity<String> sendPromotionalEmail(@RequestParam String content) {
        try {
            emailService.sendPromotionalEmailToPatients(content);
            return new ResponseEntity<>("Promotional emails sent to patients.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send promotional emails: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
