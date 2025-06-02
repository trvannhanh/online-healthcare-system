package com.can.repositories;

import com.can.pojo.Notifications;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author DELL
 */
public interface NotificationRepository {

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

    List<Notifications> getAllNotifications() throws ParseException;

    void deleteNotification(int id);

    List<Notifications> getUpcomingNotifications(Integer userId);

    void markNotificationAsRead(int notificationId, Integer userId);
}
