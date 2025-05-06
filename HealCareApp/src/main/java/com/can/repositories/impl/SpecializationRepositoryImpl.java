/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.Specialization;
import com.can.repositories.SpecializationRepository;
import jakarta.persistence.NoResultException;
import java.util.List;
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
public class SpecializationRepositoryImpl implements SpecializationRepository{
    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Override
    public Specialization getSpecializationById(int id) {
        Session session = sessionFactory.getObject().getCurrentSession();
        return session.get(Specialization.class, id);
    }

    @Override
    public List<Specialization> getAllSpecializations() {
        Session session = sessionFactory.getObject().getCurrentSession();
        Query<Specialization> query = session.createQuery("FROM Specialization", Specialization.class);
        return query.getResultList();
    }

    @Override
    public Specialization getSpecializationByName(String name) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            Query<Specialization> query = session.createQuery("FROM Specialization WHERE name = :name", Specialization.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null; // hoặc throw nếu bạn muốn
        }
    }

    @Override
    public Specialization addSpecialization(Specialization specialization) {
        Session s = this.sessionFactory.getObject().getCurrentSession();
        s.persist(specialization);
        return specialization;
    }

    @Override
    public boolean updateSpecialization(Specialization specialization) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            session.merge(specialization);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteSpecialization(int id) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            Specialization specialization = session.get(Specialization.class, id);
            if (specialization != null) {
                session.delete(specialization);
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
