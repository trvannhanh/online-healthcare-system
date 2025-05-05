package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.pojo.Notifications;
import com.can.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/notifications")
public class ApiNotificationController {

    @Autowired
    private NotificationService notificationService;

    // Thêm mới thông báo
    @PostMapping
    public ResponseEntity<Notifications> createNotification(@RequestBody Notifications notification) {
        try {
            Notifications newNotification = notificationService.addNotification(notification);
            return new ResponseEntity<>(newNotification, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Lấy thông báo theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Notifications> getNotificationById(@PathVariable Integer id) {
        Notifications notification = notificationService.getNotificationById(id);
        return notification != null ? ResponseEntity.ok(notification) : ResponseEntity.notFound().build();
    }

    // Lấy danh sách thông báo theo tiêu chí
    @GetMapping
    public ResponseEntity<List<Notifications>> getNotifications(@RequestParam Map<String, String> params) {
        try {
            List<Notifications> notifications = notificationService.getNotificationsByCriteria(params);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách thông báo theo ID người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notifications>> getNotificationsByUserId(@PathVariable Integer userId) {
        try {
            List<Notifications> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách thông báo theo ngày tạo
    @GetMapping("/createDate")
    public ResponseEntity<List<Notifications>> getNotificationsByCreateDate(@RequestParam String createAt) {
        try {
            List<Notifications> notifications = notificationService.getNotificationsByCreateDate(createAt);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách thông báo theo trạng thái xác minh và phân trang
    @GetMapping("/verification-status")
    public ResponseEntity<List<Notifications>> getNotificationsByVerificationStatus(@RequestParam boolean isVerified,
            @RequestParam(defaultValue = "0") int page) {
        try {
            List<Notifications> notifications = notificationService.getNotificationsByVerificationStatus(isVerified,
                    page);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy danh sách thông báo theo khoảng thời gian
    @GetMapping("/date-range")
    public ResponseEntity<List<Notifications>> getNotificationsByDateRange(@RequestParam Date startDate,
            @RequestParam Date endDate) {
        try {
            List<Notifications> notifications = notificationService.getNotificationsByDateRange(startDate, endDate);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cập nhật thông báo
    @PatchMapping("/{id}")
    public ResponseEntity<Notifications> updateNotification(@PathVariable Integer id,
            @RequestBody Notifications notification) {
        try {
            if (!notificationService.isNotificationExist(id)) {
                return ResponseEntity.notFound().build();
            }
            notification.setId(id);
            notificationService.updateNotificationMessage(id, notification.getMessage());
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa thông báo theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer id) {
        try {
            if (!notificationService.isNotificationExist(id)) {
                return ResponseEntity.notFound().build();
            }
            // Call service method to delete
            notificationService.updateNotificationMessage(id, null); // You can delete or set null message as your need
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Thông báo xác nhận lịch hẹn
    @PostMapping("/appointment/notify")
    public ResponseEntity<Notifications> createAppointmentNotification(@RequestBody Appointment appointment) {
        try {
            String message = String.format(
            "Xác nhận lịch hẹn với bác sĩ %s vào lúc %s.",
            appointment.getDoctor().getUser().getFullName(),
            new SimpleDateFormat("HH:mm dd/MM/yyyy").format(appointment.getAppointmentDate())
        );
            Notifications notification = new Notifications();
            notification.setMessage(message);
            notification.setUser(appointment.getPatient().getUser());
            notification.setSentAt(new Date());
            Notifications newNotification = notificationService.addNotification(notification);
            return new ResponseEntity<>(newNotification, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
