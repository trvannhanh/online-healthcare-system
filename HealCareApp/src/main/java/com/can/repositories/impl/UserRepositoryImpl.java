/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.User;
import com.can.pojo.Role;
import com.can.repositories.UserRepository;
import jakarta.persistence.NoResultException;
import java.util.List;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private LocalSessionFactoryBean sessionFactory;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User getUserById(int id) {
        Session session = sessionFactory.getObject().getCurrentSession();
        return session.get(User.class, id);
    }

    @Override
    public List<User> getAllUsers() {
        Session session = sessionFactory.getObject().getCurrentSession();
        Query<User> query = session.createQuery("FROM User", User.class);
        return query.getResultList();
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            Query<User> query = session.createQuery("FROM User WHERE username = :uname", User.class);
            query.setParameter("uname", username);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null; // hoặc throw nếu bạn muốn
        }
    }

    @Override
    public User addUser(User u) {
        Session s = this.sessionFactory.getObject().getCurrentSession();
        s.persist(u);
        
        return u;
    }

    @Override
    public boolean updateUser(User user) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
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
            Session session = sessionFactory.getObject().getCurrentSession();
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
    
    @Override
    public boolean authenticate(String username, String password) {
        User u = this.getUserByUsername(username);

        return this.passwordEncoder.matches(password, u.getPassword());
    }

    @Override 
    public boolean isEmailExist(String email) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            User user = query.getSingleResult();
            return user != null; // Nếu có kết quả trả về thì email đã tồn tại
        } catch (NoResultException ex) {
            return false; // Nếu không có kết quả, email chưa tồn tại
        }
    }

    @Override
    public String getEmailByUserId(int userId) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            Query<User> query = session.createQuery("FROM User WHERE id = :id", User.class);
            query.setParameter("id", userId);
            User user = query.getSingleResult();
            return user.getEmail(); // Trả về email của người dùng
        } catch (NoResultException ex) {
            return null; // Nếu không tìm thấy người dùng, trả về null
        }
    }

    @Override
    public List<String> getAllEmails() {
        Session session = sessionFactory.getObject().getCurrentSession();
        Query<String> query = session.createQuery("SELECT email FROM User", String.class);
        return query.getResultList(); // Trả về danh sách tất cả email
    }

    @Override
    public List<User> getUsersByRole(String role) {
        Session session = sessionFactory.getObject().getCurrentSession();
        Role roleEnum = Role.valueOf(role);
        Query<User> query = session.createQuery("FROM User WHERE role = :role", User.class);
        query.setParameter("role", roleEnum);
        return query.getResultList(); // Trả về danh sách người dùng theo vai trò
    }
}
