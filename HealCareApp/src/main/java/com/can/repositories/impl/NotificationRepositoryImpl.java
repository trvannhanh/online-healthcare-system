package com.can.repositories.impl;

import com.can.pojo.Appointment;
import com.can.pojo.Notifications;
import com.can.pojo.User;
import com.can.repositories.NotificationRepository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Date;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author DELL
 */
@Repository
@Transactional
public class NotificationRepositoryImpl implements NotificationRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Notifications> getNotificationsByCriteria(Map<String, String> params) throws ParseException {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> q = builder.createQuery(Notifications.class);
        Root<Notifications> root = q.from(Notifications.class);

        Join<Notifications, User> userJoin = root.join("user");

        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            // Lọc theo role
            String role = params.get("role");
            if (role != null && !role.isEmpty()) {
                predicates.add(builder.equal(userJoin.get("role"), role));
            }

            // Lọc theo khoảng thời gian gửi (sentAt)
            String fromDateStr = params.get("fromDate");
            String toDateStr = params.get("toDate");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if (fromDateStr != null && toDateStr != null) {
                Date fromDate = sdf.parse(fromDateStr);
                Date toDate = sdf.parse(toDateStr);
                predicates.add(builder.between(root.get("sentAt"), fromDate, toDate));
            }
        }

        if (!predicates.isEmpty()) {
            q.where(predicates.toArray(Predicate[]::new));
        }

        q.orderBy(builder.desc(root.get("sentAt"))); // sắp xếp mới nhất trước

        Query query = session.createQuery(q);

        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;
            query.setFirstResult(start);
            query.setMaxResults(PAGE_SIZE);
        }

        return query.getResultList();
    }

    @Override
    public Notifications getNotificationById(Integer id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Notifications.class, id);
    }

    @Override
    public List<Notifications> getNotificationsByUserId(Integer userId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> query = builder.createQuery(Notifications.class);
        Root<Notifications> root = query.from(Notifications.class);

        query.where(builder.equal(root.get("userId"), userId));
        Query hqlQuery = session.createQuery(query);
        return hqlQuery.getResultList();
    }

    @Override
    public List<Notifications> getNotificationsByCreateDate(String createAt) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> query = builder.createQuery(Notifications.class);
        Root<Notifications> root = query.from(Notifications.class);

        query.where(builder.equal(root.get("createAt"), createAt));
        Query hqlQuery = session.createQuery(query);
        return hqlQuery.getResultList();
    }

    @Override
    public Notifications addNotification(Notifications notification) {
        if (notification.getType() == null) {
            throw new IllegalArgumentException("Notification type is required.");
        }
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(notification);
        return notification;
    }

    @Override
    public List<Notifications> getNotificationsByVerificationStatus(boolean isVerified, int page) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> query = builder.createQuery(Notifications.class);
        Root<Notifications> root = query.from(Notifications.class);

        query.where(builder.equal(root.get("isVerified"), isVerified));
        query.orderBy(builder.asc(root.get("id")));

        Query hqlQuery = session.createQuery(query);
        int start = (page - 1) * PAGE_SIZE;
        hqlQuery.setFirstResult(start);
        hqlQuery.setMaxResults(PAGE_SIZE);

        return hqlQuery.getResultList();
    }

    @Override
    public List<Notifications> getNotificationsByUserId(int userId, int page) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> query = builder.createQuery(Notifications.class);
        Root<Notifications> root = query.from(Notifications.class);

        query.where(builder.equal(root.get("userId"), userId));
        query.orderBy(builder.asc(root.get("id")));

        Query hqlQuery = session.createQuery(query);
        int start = (page - 1) * PAGE_SIZE;
        hqlQuery.setFirstResult(start);
        hqlQuery.setMaxResults(PAGE_SIZE);

        return hqlQuery.getResultList();
    }

    @Override
    public List<Notifications> getNotificationsByDateRange(Date startDate, Date endDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> query = builder.createQuery(Notifications.class);
        Root<Notifications> root = query.from(Notifications.class);

        query.where(builder.between(root.get("sentAt"), startDate, endDate));
        Query hqlQuery = session.createQuery(query);
        return hqlQuery.getResultList();
    }

    @Override
    public boolean isNotificationExist(int notificationId) {
        Session session = this.factory.getObject().getCurrentSession();
        Notifications notification = session.get(Notifications.class, notificationId);
        return notification != null;
    }

    @Override
    public void updateNotificationMessage(int notificationId, String message) {
        Session session = this.factory.getObject().getCurrentSession();
        Notifications notification = session.get(Notifications.class, notificationId);
        if (notification != null && (notification.getSentAt() == null ||
                notification.getSentAt().after(new Date()))) {
            notification.setMessage(message);
            session.update(notification);
        }
    }

    @Override
    public List<Notifications> getAllNotifications() throws ParseException {
        return getNotificationsByCriteria(null);
    }

    @Override
    public void deleteNotification(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        Notifications notification = session.get(Notifications.class, id);
        if (notification != null)
            session.remove(notification);
    }

    @Override
    public List<Notifications> getUpcomingAppointmentNotifications(Integer userId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> q = builder.createQuery(Notifications.class);
        Root<Notifications> root = q.from(Notifications.class);

        // Join với bảng user để lọc theo userId
        Join<Notifications, User> userJoin = root.join("user");

        // Lấy các thông báo lịch hẹn trong 1-2 ngày tới
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.setTime(today);

        cal.add(Calendar.DAY_OF_MONTH, 2); // Lấy thông báo đến 2 ngày sau
        Date twoDaysLater = cal.getTime();

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(userJoin.get("id"), userId));
        predicates.add(builder.equal(root.get("type"), "APPOINTMENT")); // Giả sử có field type để phân loại thông báo
        predicates.add(builder.equal(root.get("isRead"), false)); // Chưa đọc

        // Lọc thông báo có ngày hẹn nằm trong khoảng từ hiện tại đến 2 ngày sau
        // Giả sử có field appointmentDate lưu ngày hẹn, hoặc có thể dùng field khác tùy
        // vào cấu trúc dữ liệu
        predicates.add(builder.between(root.get("appointmentDate"), today, twoDaysLater));

        q.where(predicates.toArray(new Predicate[0]));
        q.orderBy(builder.asc(root.get("appointmentDate"))); // Sắp xếp theo thời gian lịch hẹn

        Query query = session.createQuery(q);
        return query.getResultList();
    }

    @Override
    public void markNotificationAsRead(int notificationId, Integer userId) {
        Session session = this.factory.getObject().getCurrentSession();

        // Lấy thông báo theo ID và userId để đảm bảo người dùng chỉ có thể đánh dấu
        // đã đọc các thông báo của chính họ
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> q = builder.createQuery(Notifications.class);
        Root<Notifications> root = q.from(Notifications.class);

        Join<Notifications, User> userJoin = root.join("user");

        q.where(
                builder.and(
                        builder.equal(root.get("id"), notificationId),
                        builder.equal(userJoin.get("id"), userId)));

        Query query = session.createQuery(q);

        try {
            Notifications notification = (Notifications) query.getSingleResult();

            // Đánh dấu đã đọc và cập nhật
            notification.setIsRead(true);
            session.update(notification);
        } catch (NoResultException e) {
            // Không tìm thấy thông báo hoặc thông báo không thuộc về người dùng này
            // Có thể ghi log hoặc xử lý theo nhu cầu
        }
    }

}
