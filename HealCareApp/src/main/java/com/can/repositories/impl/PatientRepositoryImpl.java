/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

/**
 *
 * @author Giidavibe
 */
import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.PatientRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PatientRepositoryImpl implements PatientRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    private static final int PAGE_SIZE = 10;

    @Override
    public boolean isUsernameExists(Session session, String username) {
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<User> root = q.from(User.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("username"), username));
        Long count = session.createQuery(q).getSingleResult();
        return count > 0;
    }

    @Override
    public List<Patient> getPatients(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Patient> q = b.createQuery(Patient.class);
        Root<Patient> root = q.from(Patient.class);
        root.fetch("user");

        Join<Patient, User> userJoin = root.join("user");

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            String patientName = params.get("patientName");
            if (patientName != null && !patientName.isEmpty()) {
                String searchPattern = String.format("%%%s%%", patientName.toLowerCase());
                Predicate firstNamePredicate = b.like(
                        b.lower(userJoin.get("firstName")),
                        searchPattern
                );
                Predicate lastNamePredicate = b.like(
                        b.lower(userJoin.get("lastName")),
                        searchPattern
                );
                predicates.add(b.or(firstNamePredicate, lastNamePredicate));
            }

            String insuranceNumber = params.get("insuranceNumber");
            if (insuranceNumber != null && !insuranceNumber.isEmpty()) {
                predicates.add(b.equal(
                        b.lower(root.get("insuranceNumber")),
                        insuranceNumber.toLowerCase()
                ));
            }

            String dateOfBirth = params.get("dateOfBirth");
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
                    predicates.add(b.equal(root.get("dateOfBirth"), dob));
                } catch (Exception e) {
                    throw new RuntimeException("Định dạng ngày không hợp lệ: " + e.getMessage(), e);
                }
            }

            q.where(predicates.toArray(Predicate[]::new));
        }

        q.orderBy(b.asc(root.get("id")));

        Query<Patient> query = s.createQuery(q);

        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;
            query.setFirstResult(start);
            query.setMaxResults(PAGE_SIZE);
        }

        return query.getResultList();
    }

    public List<Patient> getAllPatients() {
        return getPatients(null);
    }

    @Override
    public Patient getPatientById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Patient> q = b.createQuery(Patient.class
        );
        Root<Patient> root = q.from(Patient.class
        );

        Join<Patient, User> userJoin = root.join("user");

        q.where(b.equal(userJoin.get("id"), id));

        Query query = s.createQuery(q);
        return (Patient) query.getSingleResult();

    }

    @Override
    public Patient addPatient(Patient patient) {

        Session s = this.factory.getObject().getCurrentSession();

        s.persist(patient);
        return patient;

    }

    @Override
    public Patient updatePatient(Patient patient) {
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();

        Patient existingPatient = s.get(Patient.class, patient.getId());
        if (existingPatient == null) {
            throw new RuntimeException("Bệnh nhân với Id " + patient.getId() + " không tìm thấy");
        }

        if (patient.getUser() == null) {
            throw new RuntimeException("Thông tin người dùng là cần thiết");
        }

        if (patient.getUser().getId() != patient.getId()) {
            throw new RuntimeException("Id user phải khớp với id bệnh nhân");
        }

        User existingUser = s.get(User.class, patient.getUser().getId());
        if (!existingUser.getUsername().equals(patient.getUser().getUsername()) && isUsernameExists(s, patient.getUser().getUsername())) {
            throw new RuntimeException("Username '" + patient.getUser().getUsername() + "' đã tồn tại");
        }

        s.merge(patient.getUser());
        s.flush();

        Patient updatedPatient = (Patient) s.merge(patient);
        return updatedPatient;

    }

    @Override
    public void deletePatient(Integer id) {
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();
        transaction = s.beginTransaction();

        Patient patient = s.get(Patient.class, id);
        if (patient == null) {
            throw new RuntimeException("Bệnh nhân với id " + id + " không tìm thấy");
        }

        if (patient.getUser() != null) {
            s.remove(patient.getUser());
        }

        s.remove(patient);
        transaction.commit();

    }

}
