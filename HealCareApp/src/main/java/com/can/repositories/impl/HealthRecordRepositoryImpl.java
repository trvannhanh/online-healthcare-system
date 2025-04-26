/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;
import com.can.pojo.HealthRecord;
import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.HealthRecordRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 *
 * @author DELL
 */
@Repository
@Transactional
public class HealthRecordRepositoryImpl implements HealthRecordRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    // Lấy danh sách hồ sơ sức khỏe với bộ lọc
    @Override
    public List<HealthRecord> getHealthRecords(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("patient").fetch("user");

        Join<HealthRecord, Patient> patientJoin = root.join("patient");
        Join<Patient, User> patientUserJoin = patientJoin.join("user");

        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            String patientName = params.get("patientName");
            if (patientName != null && !patientName.isEmpty()) {
                String searchPattern = "%" + patientName.toLowerCase() + "%";
                Predicate firstNamePredicate = b.like(b.lower(patientUserJoin.get("firstName")), searchPattern);
                Predicate lastNamePredicate = b.like(b.lower(patientUserJoin.get("lastName")), searchPattern);
                predicates.add(b.or(firstNamePredicate, lastNamePredicate));
            }

            String recordDate = params.get("recordDate");
            if (recordDate != null && !recordDate.isEmpty()) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(recordDate);
                    predicates.add(b.equal(b.function("DATE", Date.class, root.get("recordDate")), date));
                } catch (Exception e) {
                    throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd");
                }
            }

            String patientId = params.get("patientId");
            if (patientId != null && !patientId.isEmpty()) {
                predicates.add(b.equal(patientJoin.get("id"), Integer.parseInt(patientId)));
            }
        }

        if (!predicates.isEmpty()) {
            q.where(predicates.toArray(new Predicate[0]));
        }

        q.orderBy(b.asc(root.get("recordDate")));

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
    public List<HealthRecord> getAllHealthRecords() {
        return getHealthRecords(null);
    }

    @Override
    public HealthRecord getHealthRecordById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("patient").fetch("user");

        q.where(b.equal(root.get("id"), id));

        Query query = s.createQuery(q);
        return (HealthRecord) query.getSingleResult();
    }

    @Override
    public HealthRecord addHealthRecord(HealthRecord healthRecord) {
        Session s = this.factory.getObject().getCurrentSession();

        if (healthRecord.getPatient() == null) {
            throw new RuntimeException("Patient is required for a Health Record");
        }

        Patient patient = s.get(Patient.class, healthRecord.getPatient().getId());
        if (patient == null) {
            throw new RuntimeException("Patient with ID " + healthRecord.getPatient().getId() + " not found");
        }

        s.persist(healthRecord);
        return healthRecord;
    }

    @Override
    public HealthRecord updateHealthRecord(HealthRecord healthRecord) {
        Session s = this.factory.getObject().getCurrentSession();

        HealthRecord existing = s.get(HealthRecord.class, healthRecord.getId());
        if (existing == null) {
            throw new RuntimeException("Health Record with ID " + healthRecord.getId() + " not found");
        }

        if (healthRecord.getPatient() == null) {
            throw new RuntimeException("Patient is required for a Health Record");
        }

        Patient patient = s.get(Patient.class, healthRecord.getPatient().getId());
        if (patient == null) {
            throw new RuntimeException("Patient with ID " + healthRecord.getPatient().getId() + " not found");
        }

        return (HealthRecord) s.merge(healthRecord);
    }

    @Override
    public void deleteHealthRecord(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        HealthRecord healthRecord = session.get(HealthRecord.class, id);
        if (healthRecord == null) {
            throw new RuntimeException("Health Record with ID " + id + " not found");
        }
        session.remove(healthRecord);
    }

    @Override
    public List<HealthRecord> getHealthRecordsByPatient(int patientId, int page) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("patient").fetch("user");

        q.where(b.equal(root.get("patient").get("id"), patientId));
        q.orderBy(b.asc(root.get("recordDate")));

        Query query = s.createQuery(q);
        int start = (page - 1) * PAGE_SIZE;
        query.setFirstResult(start);
        query.setMaxResults(PAGE_SIZE);

        return query.getResultList();
    }
}
