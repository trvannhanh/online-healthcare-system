/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.ChatAttachment;
import com.can.repositories.ChatAttachmentRepository;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
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
public class ChatAttachmentRepositoryImpl implements ChatAttachmentRepository {
    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Override
    public ChatAttachment getChatAttachmentById(int id) {
        Session session = sessionFactory.getObject().getCurrentSession();
        return session.get(ChatAttachment.class, id);
    }

    @Override
    public ChatAttachment getChatAttachmentByMessageId(String messageId) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            // Queries for a chat attachment by its associated message ID
            Query<ChatAttachment> query = session.createQuery(
                "FROM ChatAttachment WHERE messageId = :messageId", ChatAttachment.class
            );
            query.setParameter("messageId", messageId);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            // Returns null if no attachment is found for the given message ID
            return null;
        }
    }

    @Override
    public ChatAttachment addChatAttachment(ChatAttachment attachment) {
        Session session = sessionFactory.getObject().getCurrentSession();
        session.persist(attachment);
        return attachment;
    }

    @Override
    public boolean deleteChatAttachment(int id) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            ChatAttachment attachment = session.get(ChatAttachment.class, id);
            if (attachment != null) {
                session.remove(attachment);
                return true;
            }
            // Returns false if no attachment is found for the given ID
            return false;
        } catch (Exception ex) {
            // Logs any unexpected errors during deletion
            ex.printStackTrace();
            return false;
        }
    }
}
