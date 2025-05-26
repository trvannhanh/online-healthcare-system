package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.services.EmailService;
import com.can.services.UserService;
import com.can.pojo.User;
import com.can.pojo.Role;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api")
@CrossOrigin
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    // Gửi email test thử (đã test thành công)
    @PostMapping("/email/test-email")
    public ResponseEntity<String> testEmail(
            @RequestParam("to") String to,
            @RequestParam(name = "subject", defaultValue = "Test Email từ HealCare") String subject,
            @RequestParam(name = "body", defaultValue = "Đây là email test từ hệ thống.") String body) {
        try {
            // Chỉ cho phép gửi đến email cụ thể để tránh lạm dụng
            // Thay your-email@gmail.com bằng email thật của bạn để test
            if (!to.equals("chitrannguyenlinh@gmail.com")) {
                return new ResponseEntity<>(
                        "For security reasons, this test endpoint only allows sending to predefined email addresses.",
                        HttpStatus.FORBIDDEN);
            }

            emailService.sendEmail(to, subject, body);
            return new ResponseEntity<>("Test email sent successfully to " + to, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // In chi tiết lỗi vào log
            return new ResponseEntity<>("Failed to send test email: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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

    // Gửi email xác nhận lịch hẹn
    @PostMapping("/secure/email/appointment/confirm")
    public ResponseEntity<?> sendAppointmentConfirmation(@RequestBody Appointment appointment) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);

            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Không tìm thấy thông tin người dùng");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // Gọi service để xử lý việc gửi email - mọi logic đều nằm trong service
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

    // Gửi thông báo nhắc lịch hẹn
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

    // Gửi email khuyến mãi đến tất cả bệnh nhân
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
}
