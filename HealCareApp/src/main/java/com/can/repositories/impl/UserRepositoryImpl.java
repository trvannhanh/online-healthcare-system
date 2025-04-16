/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.User;
import com.can.repositories.UserRepository;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Giidavibe
 */

@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository{

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public User getUserById(int id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(User.class, id);
    }

    @Override
    public List<User> getAllUsers() {
        Session session = sessionFactory.getCurrentSession();
        Query<User> query = session.createQuery("FROM User", User.class);
        return query.getResultList();
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<User> query = session.createQuery("FROM User WHERE username = :uname", User.class);
            query.setParameter("uname", username);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null; // hoặc throw nếu bạn muốn
        }
    }

    @Override
    public boolean addUser(User user) {
        try {
            Session session = sessionFactory.getCurrentSession();
            session.save(user);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateUser(User user) {
        try {
            Session session = sessionFactory.getCurrentSession();
            session.merge(user);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
 
    @Override
    public boolean deleteUser(int id) {
        try {
            Session session = sessionFactory.getCurrentSession();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
