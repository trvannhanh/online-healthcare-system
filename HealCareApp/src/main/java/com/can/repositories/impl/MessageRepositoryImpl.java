package com.can.repositories.impl;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import com.can.pojo.Messages;
import com.can.pojo.User;
import java.util.Date;
import com.can.repositories.MessageRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class MessageRepositoryImpl implements MessageRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public Messages getMessageById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(Messages.class, id);
    }

    @Override
    public List<Messages> getMessagesBySender(User sender) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Messages> q = b.createQuery(Messages.class);
        Root<Messages> root = q.from(Messages.class);

        q.where(b.equal(root.get("sender"), sender));
        q.orderBy(b.desc(root.get("timestamp")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Messages> getMessagesByReceiver(User receiver) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Messages> q = b.createQuery(Messages.class);
        Root<Messages> root = q.from(Messages.class);

        q.where(b.equal(root.get("receiver"), receiver));
        q.orderBy(b.desc(root.get("timestamp")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Messages> getMessages(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Messages> q = b.createQuery(Messages.class);
        Root<Messages> root = q.from(Messages.class);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo nội dung tin nhắn
            String content = params.get("content");
            if (content != null && !content.isEmpty()) {
                String searchPattern = String.format("%%%s%%", content.toLowerCase());
                predicates.add(b.like(b.lower(root.get("content")), searchPattern));
            }

            // Lọc theo ID người gửi
            String senderId = params.get("senderId");
            if (senderId != null && !senderId.isEmpty()) {
                predicates.add(b.equal(root.get("sender").get("id"), Integer.parseInt(senderId)));
            }

            // Lọc theo ID người nhận
            String receiverId = params.get("receiverId");
            if (receiverId != null && !receiverId.isEmpty()) {
                predicates.add(b.equal(root.get("receiver").get("id"), Integer.parseInt(receiverId)));
            }

            // Lọc theo ngày gửi
            String timestamp = params.get("timestamp");
            if (timestamp != null && !timestamp.isEmpty()) {
                try {
                    Date ts = new Date(Long.parseLong(timestamp));
                    predicates.add(b.equal(root.get("timestamp"), ts));
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Invalid timestamp format");
                }
            }

            q.where(predicates.toArray(Predicate[]::new));
        }

        q.orderBy(b.desc(root.get("timestamp")));

        Query query = s.createQuery(q);

        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;

            query.setFirstResult(start);
            query.setMaxResults(PAGE_SIZE);
        }

        return query.getResultList();
    }

    @Override
    public List<Messages> getMessagesByTimestamp(Date timestamp) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Messages> q = b.createQuery(Messages.class);
        Root<Messages> root = q.from(Messages.class);

        q.where(b.equal(root.get("timestamp"), timestamp));
        q.orderBy(b.desc(root.get("timestamp")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }
}
