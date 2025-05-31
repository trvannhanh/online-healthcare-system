package com.can.controllers;

import com.can.pojo.Notifications;
import com.can.services.NotificationService;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/secure/notifications/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable("id") int id) {
        try {
            // Lấy thông báo theo ID
            Notifications notification = notificationService.getNotificationById(id);

            return new ResponseEntity<>(notification, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy thông báo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/secure/notifications/all")
    public ResponseEntity<?> getAllNotifications(Principal principal) {
        try {

            List<Notifications> notifications = notificationService
                    .getNotificationsByUser(principal.getName());
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy thông báo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Lấy thông báo sắp tới cho người dùng hiện tại
    @GetMapping("/secure/notifications/upcoming")
    public ResponseEntity<?> getUpcomingNotifications(Principal principal) {

        try {
            List<Notifications> notifications = notificationService
                    .getUpcomingAppointmentNotifications(principal.getName());
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi lấy thông báo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Đánh dấu thông báo đã đọc
    @PatchMapping("/secure/notifications/{id}/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable("id") int id, Principal principal) {

        try {
            notificationService.markNotificationAsRead(id, principal.getName());
            return new ResponseEntity<>(Map.of("status", "success"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi cập nhật trạng thái thông báo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
