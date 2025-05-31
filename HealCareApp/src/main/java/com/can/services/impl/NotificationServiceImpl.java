package com.can.services.impl;

import com.can.pojo.User;
import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.NotificationType;
import com.can.pojo.Notifications;
import com.can.repositories.NotificationRepository;
import com.can.services.AppointmentService;
import com.can.services.NotificationService;
import com.can.services.UserService;

import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.ParseException;

/**
 *
 * @author DELL
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Override
    public List<Notifications> getNotificationsByCriteria(Map<String, String> params) throws ParseException {
        return this.notificationRepository.getNotificationsByCriteria(params);
    }

    @Override
    public Notifications getNotificationById(Integer id) {
        return this.notificationRepository.getNotificationById(id);
    }

    @Override
    public List<Notifications> getNotificationsByUser(String username) {
        // Lấy thông tin người dùng
        User user = userService.getUserByUsername(username);

        // Kiểm tra xem người dùng có tồn tại không
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }

        // Chỉ cho phép bác sĩ hoặc bệnh nhân xem thông báo
        if (!user.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ có bệnh nhân mới có thể xem thông báo");
        }
        return this.notificationRepository.getNotificationsByUserId(user.getId());
    }

    @Override
    public List<Notifications> getNotificationsByCreateDate(String createAt) {
        return this.notificationRepository.getNotificationsByCreateDate(createAt);
    }

    @Override
    public Notifications addNotification(Notifications notification) {
        return this.notificationRepository.addNotification(notification);
    }

    @Override
    public List<Notifications> getNotificationsByVerificationStatus(boolean isVerified, int page) {
        return this.notificationRepository.getNotificationsByVerificationStatus(isVerified, page);
    }

    @Override
    public List<Notifications> getNotificationsByUserId(int userId, int page) {
        return this.notificationRepository.getNotificationsByUserId(userId, page);
    }

    @Override
    public List<Notifications> getNotificationsByDateRange(Date startDate, Date endDate) {
        return this.notificationRepository.getNotificationsByDateRange(startDate, endDate);
    }

    @Override
    public boolean isNotificationExist(int notificationId) {
        return this.notificationRepository.isNotificationExist(notificationId);
    }

    @Override
    public void updateNotificationMessage(int notificationId, String message) {
        this.notificationRepository.updateNotificationMessage(notificationId, message);
    }

    @Override
    public void deleteNotification(int id) {
        this.notificationRepository.deleteNotification(id);
    }

    @Override
    public List<Notifications> getUpcomingAppointmentNotifications(String username) {
        // Lấy thông tin người dùng từ username
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }

        // Gọi repository để lấy thông báo lịch hẹn sắp tới
        return this.notificationRepository.getUpcomingAppointmentNotifications(user.getId());
    }

    @Override
    public void markNotificationAsRead(int notificationId, String username) {
        // Lấy thông tin người dùng từ username
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }

        this.notificationRepository.markNotificationAsRead(notificationId, user.getId());
    }

    @Override
    public Notifications createAppointmentNotification(int appointmentId, String username) {
        // Lấy thông tin người dùng
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với username: " + username);
        }

        // Lấy thông tin lịch hẹn
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("Không tìm thấy lịch hẹn với ID: " + appointmentId);
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ tạo thông báo cho lịch hẹn đã được xác nhận");
        }

        // Tạo thông báo mới
        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setType(NotificationType.APPOINTMENT);
        notification.setIsRead(false);

        // Thiết lập ngày gửi thông báo 2 ngày trước lịch hẹn
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointment.getAppointmentDate());
        cal.add(Calendar.DAY_OF_MONTH, -2); // Trừ 2 ngày
        notification.setSentAt(cal.getTime()); // Sửa lại: setSentAt thay vì getSentAt
        // Định dạng ngày giờ lịch hẹn
        String formattedDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(appointment.getAppointmentDate());

        // Tạo nội dung thông báo
        String doctorName = appointment.getDoctor().getUser().getFirstName() + " "
                + appointment.getDoctor().getUser().getLastName();
        String message = String.format("Bạn có lịch hẹn với bác sĩ: %s vào ngày %s", doctorName, formattedDate);
        notification.setMessage(message);

        // Lưu và trả về thông báo
        return this.notificationRepository.addNotification(notification);
    }

}
