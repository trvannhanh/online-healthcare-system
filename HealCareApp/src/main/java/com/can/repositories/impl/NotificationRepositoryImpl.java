package com.can.repositories.impl;

import com.can.pojo.Notifications;
import com.can.pojo.User;
import com.can.repositories.NotificationRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
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
 * @author Giidavibe
 */
@Repository
@Transactional
public class NotificationRepositoryImpl implements NotificationRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Notifications> getNotificationsByCriteria(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Notifications> query = builder.createQuery(Notifications.class);
        Root<Notifications> root = query.from(Notifications.class);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo trạng thái đọc
            String isRead = params.get("isRead");
            if (isRead != null) {
                predicates.add(builder.equal(root.get("isRead"), Boolean.parseBoolean(isRead)));
            }

            // Lọc theo ngày tạo
            String createAt = params.get("createAt");
            if (createAt != null) {
                predicates.add(builder.equal(root.get("createAt"), createAt));
            }

            // Áp dụng các điều kiện lọc
            query.where(predicates.toArray(new Predicate[0]));
        }

        Query hqlQuery = session.createQuery(query);

        // Phân trang nếu có
        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;
            hqlQuery.setFirstResult(start);
            hqlQuery.setMaxResults(PAGE_SIZE);
        }

        return hqlQuery.getResultList();
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
        Session session = this.factory.getObject().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.save(notification);
        transaction.commit();
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
        Transaction transaction = session.beginTransaction();
        Notifications notification = session.get(Notifications.class, notificationId);
        if (notification != null) {
            notification.setMessage(message);
            session.update(notification);
        }
        transaction.commit();
    }

}
