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
public class ChatAttachmentRepositoryImpl implements ChatAttachmentRepository{
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
            Query<ChatAttachment> query = session.createQuery(
                "FROM ChatAttachment WHERE messageId = :messageId", ChatAttachment.class
            );
            query.setParameter("messageId", messageId);
            return query.getSingleResult();
        } catch (NoResultException ex) {
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
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
