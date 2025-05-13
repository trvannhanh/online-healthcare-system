package com.can.controllers;

import com.can.pojo.Notifications;
import com.can.services.NotificationService;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class ApiNotificationController {

    @Autowired
    private NotificationService notificationService;

    // Xóa thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable("id") int id) {
        try {
            Notifications n = notificationService.getNotificationById(id);
            if (n == null)
                return new ResponseEntity<>("Không tìm thấy thông báo", HttpStatus.NOT_FOUND);

            // Chỉ xóa nếu chưa gửi
            if (n.getSentAt() == null || n.getSentAt().after(new Date())) {
                notificationService.deleteNotification(id);
                return new ResponseEntity<>("Xóa thành công", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("Không thể xóa thông báo đã gửi", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi xóa thông báo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
