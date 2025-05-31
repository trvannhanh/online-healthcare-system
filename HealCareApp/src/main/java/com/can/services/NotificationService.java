package com.can.services;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.can.pojo.Appointment;
import com.can.pojo.Notifications;

/**
 *
 * @author DELL
 */
public interface NotificationService {
    List<Notifications> getNotificationsByCriteria(Map<String, String> params) throws ParseException;

    Notifications getNotificationById(Integer id);

    List<Notifications> getNotificationsByUserId(Integer userId);

    List<Notifications> getNotificationsByCreateDate(String createAt);

    Notifications addNotification(Notifications notification);

    List<Notifications> getNotificationsByVerificationStatus(boolean isVerified, int page);

    List<Notifications> getNotificationsByUserId(int userId, int page);

    List<Notifications> getNotificationsByDateRange(Date startDate, Date endDate);

    boolean isNotificationExist(int notificationId);

    void updateNotificationMessage(int notificationId, String message);

    void deleteNotification(int id);

    List<Notifications> getUpcomingAppointmentNotifications(String username);

    void markNotificationAsRead(int notificationId, String username);
}
