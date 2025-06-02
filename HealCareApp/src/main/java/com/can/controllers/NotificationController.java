package com.can.controllers;

import com.can.pojo.Notifications;
import com.can.pojo.User;
import com.can.services.NotificationService;
import com.can.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.beans.PropertyEditorSupport;
import java.text.ParseException;
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

        return "notifications/notifications"; 
    }

    @GetMapping("/notifications/add")
    public String showAddForm(Model model) {
        model.addAttribute("notification", new Notifications());
        model.addAttribute("users", userService.getAllUsers());
        return "notifications/notification_add"; 
    }

    @PostMapping("/notifications/add")
    public String addNotification(
            @ModelAttribute("notification") Notifications notificationFromForm) {
        List<User> patients = userService.getUsersByRole("PATIENT"); 

        for (User patient : patients) {
            Notifications notification = new Notifications();
            notification.setUser(patient);
            notification.setType(notificationFromForm.getType());
            notification.setMessage(notificationFromForm.getMessage());
            notification.setSentAt(notificationFromForm.getSentAt());

            this.notificationService.addNotification(notification);
        }

        return "redirect:/notifications";
    }

    @GetMapping("/notifications/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Notifications notification = this.notificationService.getNotificationById(id);
        boolean canEdit = false;
        String alertMessage = null;

        if (notification != null) {
            Date sentAt = notification.getSentAt();
            if (sentAt == null || sentAt.after(new Date())) {
                canEdit = true;
            } else {
                alertMessage = "Thông báo đã được gửi, không thể chỉnh sửa.";
            }
        } else {
            alertMessage = "Không tìm thấy thông báo.";
        }

        model.addAttribute("notification", notification);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("alertMessage", alertMessage);
        return "notifications/notification_edit";
    }

    @PostMapping("/notifications/edit/{id}")
    public String editNotification(@PathVariable("id") int id,
            @RequestParam("message") String message) {
        Notifications notification = this.notificationService.getNotificationById(id);
        if (notification != null) {
            this.notificationService.updateNotificationMessage(id, message); 
        }
        return "redirect:/notifications"; 
    }

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