/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.Hospital;
import com.can.repositories.HospitalRepository;
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
public class HospitalRepositoryImpl implements HospitalRepository{
    
    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Override
    public Hospital getHospitalById(int id) {
        Session session = sessionFactory.getObject().getCurrentSession();
        return session.get(Hospital.class, id);
    }

    @Override
    public List<Hospital> getAllHospitals() {
        Session session = sessionFactory.getObject().getCurrentSession();
        Query<Hospital> query = session.createQuery("FROM Hospital", Hospital.class);
        return query.getResultList();
    }

    @Override
    public Hospital getHospitalByName(String name) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
        Query<Hospital> query = session.createQuery("FROM Hospital WHERE name = :name", Hospital.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Hospital addHospital(Hospital hospital) {
        Session s = this.sessionFactory.getObject().getCurrentSession();
        s.persist(hospital);
        return hospital;
    }

    @Override
    public boolean updateHospital(Hospital hospital) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            session.merge(hospital);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteHospital(int id) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();
            Hospital hospital = session.get(Hospital.class, id);
            if (hospital != null) {
                session.remove(hospital);
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
