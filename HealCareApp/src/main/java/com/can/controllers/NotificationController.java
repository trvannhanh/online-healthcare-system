package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.pojo.NotificationType;
import com.can.pojo.Notifications;
import com.can.pojo.User;
import com.can.services.NotificationService;
import com.can.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.management.Notification;

import org.springframework.stereotype.Controller;

/**
 *
 * @author DELL
 */
@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/notifications")
    public String showNotifications(Model model, @RequestParam Map<String, String> params) throws ParseException {
        List<Notifications> notifications = this.notificationService.getNotificationsByCriteria(params);

        model.addAttribute("notifications", notifications);
        model.addAttribute("users", this.userService.getAllUsers());

        return "notifications/notifications"; // Tên file HTML hiển thị danh sách thông báo
    }

    @GetMapping("/notifications/add")
    public String showAddForm(Model model) {
        model.addAttribute("notification", new Notifications());
        model.addAttribute("users", userService.getAllUsers());
        return "notifications/notification_add"; // Tên file HTML hiển thị form thêm thông báo
    }

    @PostMapping("/notifications/add")
    public String addNotification(
        @ModelAttribute("notification") Notifications notificationFromForm) {
        List<User> patients = userService.getUsersByRole("PATIENT"); // hoặc Enum nếu bạn dùng

        for (User patient : patients) {
            Notifications notification = new Notifications();
            notification.setUser(patient);
            notification.setType(notificationFromForm.getType());
            notification.setMessage(notificationFromForm.getMessage());
            notification.setSentAt(notificationFromForm.getSentAt());

            this.notificationService.addNotification(notification);
        }

        return "redirect:/notifications"; // Quay lại danh sách thông báo
    }

    @GetMapping("/notifications/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Notifications notification = this.notificationService.getNotificationById(id);
        model.addAttribute("notification", notification);
        return "notifications/notification_edit"; // Tên file HTML hiển thị form chỉnh sửa thông báo
    }

    @PostMapping("/notifications/edit/{id}")
    public String editNotification(@PathVariable("id") int id,
            @RequestParam("message") String message) {
        // Lấy thông báo từ cơ sở dữ liệu
        Notifications notification = this.notificationService.getNotificationById(id);
        if (notification != null) {
            this.notificationService.updateNotificationMessage(id, message); // Lưu thay đổi
        }
        return "redirect:/notifications"; // Quay lại danh sách thông báo
    }

    @PutMapping("/notifications/update/{id}")

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Notification.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                Notifications n = notificationService.getNotificationById(Integer.parseInt(text));
                setValue(n);
            }
        });
    }
}