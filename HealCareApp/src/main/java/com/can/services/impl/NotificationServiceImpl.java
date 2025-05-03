package com.can.services.impl;

import com.can.pojo.Notifications;

import com.can.repositories.NotificationRepository;
import com.can.services.NotificationService;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Notifications> getNotificationsByCriteria(Map<String, String> params) {
        return this.notificationRepository.getNotificationsByCriteria(params);
    }

    @Override
    public Notifications getNotificationById(Integer id) {
        return this.notificationRepository.getNotificationById(id);
    }

    @Override
    public List<Notifications> getNotificationsByUserId(Integer userId) {
        return this.notificationRepository.getNotificationsByUserId(userId);
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
    
}
