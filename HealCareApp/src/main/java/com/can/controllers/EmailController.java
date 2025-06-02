package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.services.EmailService;
import com.can.services.UserService;
import com.can.pojo.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @PostMapping("/email/send-html")
    public ResponseEntity<?> sendHtmlEmail(
            @RequestParam(name = "to") String to,
            @RequestParam(name = "subject") String subject,
            @RequestParam(name = "body") String htmlContent) {
        try {
            if (!to.equals("chitrannguyenlinh@gmail.com")) {
                return new ResponseEntity<>(
                        "For security reasons, this test endpoint only allows sending to predefined email addresses.",
                        HttpStatus.FORBIDDEN);
            }

            emailService.sendHtmlEmail(to, subject, htmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gửi email HTML thành công");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Lỗi khi gửi email HTML: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/secure/email/appointment/confirm")
    public ResponseEntity<?> sendAppointmentConfirmation(@RequestBody Appointment appointment) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);

            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Không tìm thấy thông tin người dùng");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            emailService.sendAppointmentConfirmationEmail(appointment, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gửi email xác nhận lịch hẹn thành công");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Lỗi khi gửi email xác nhận: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/appointment/notify")
    public ResponseEntity<String> sendAppointmentNotification(@RequestBody Appointment appointment) {
        try {
            emailService.sendAppointmentNotificationEmail(appointment);
            return new ResponseEntity<>("Appointment notification email sent.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send notification email: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/promotion")
    public ResponseEntity<String> sendPromotionalEmail(@RequestParam String content) {
        try {
            emailService.sendPromotionalEmailToPatients(content);
            return new ResponseEntity<>("Promotional emails sent to patients.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to send promotional emails: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/email/test-email")
    public ResponseEntity<String> testEmail(
            @RequestParam("to") String to,
            @RequestParam(name = "subject", defaultValue = "Test Email từ HealCare") String subject,
            @RequestParam(name = "body", defaultValue = "Đây là email test từ hệ thống.") String body) {
        try {
            if (!to.equals("chitrannguyenlinh@gmail.com")) {
                return new ResponseEntity<>(
                        "For security reasons, this test endpoint only allows sending to predefined email addresses.",
                        HttpStatus.FORBIDDEN);
            }

            emailService.sendEmail(to, subject, body);
            return new ResponseEntity<>("Test email sent successfully to " + to, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); 
            return new ResponseEntity<>("Failed to send test email: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
