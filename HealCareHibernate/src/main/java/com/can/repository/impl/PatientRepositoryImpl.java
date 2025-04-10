/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repository.impl;

/**
 *
 * @author Giidavibe
 */

import com.can.healcarehibernate.HibernateUtils;
import com.can.pojo.Patient;
import com.can.pojo.User;
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
import org.hibernate.resource.transaction.spi.TransactionStatus;

public class PatientRepositoryImpl {

    private static final int PAGE_SIZE = 10;

    // Kiểm tra xem username đã tồn tại hay chưa
    private boolean isUsernameExists(Session session, String username) {
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<User> root = q.from(User.class);
        q.select(b.count(root));
        q.where(b.equal(root.get("username"), username));
        Long count = session.createQuery(q).getSingleResult();
        return count > 0;
    }

    // Lấy danh sách bệnh nhân với bộ lọc
    public List<Patient> getPatients(Map<String, String> params) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Patient> q = b.createQuery(Patient.class);
            Root<Patient> root = q.from(Patient.class);
            root.fetch("user");

            Join<Patient, User> userJoin = root.join("user");

            if (params != null) {
                List<Predicate> predicates = new ArrayList<>();

                // Lọc theo tên bệnh nhân
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

                // Lọc theo số bảo hiểm
                String insuranceNumber = params.get("insuranceNumber");
                if (insuranceNumber != null && !insuranceNumber.isEmpty()) {
                    predicates.add(b.equal(
                            b.lower(root.get("insuranceNumber")),
                            insuranceNumber.toLowerCase()
                    ));
                }

                // Lọc theo ngày sinh
                String dateOfBirth = params.get("dateOfBirth");
                if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                    try {
                        Date dob = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
                        predicates.add(b.equal(root.get("dateOfBirth"), dob));
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format for dateOfBirth: " + e.getMessage(), e);
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
    }

    // Lấy tất cả bệnh nhân
    public List<Patient> getAllPatients() {
        return getPatients(null);
    }

    // Lấy bệnh nhân theo ID
    public Patient getPatientById(Integer id) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Patient> q = b.createQuery(Patient.class);
            Root<Patient> root = q.from(Patient.class);
            root.fetch("user");

            q.where(b.equal(root.get("id"), id));

            Query<Patient> query = s.createQuery(q);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get patient by ID: " + e.getMessage(), e);
        }
    }

    // Thêm bệnh nhân mới
    public Patient addPatient(Patient patient) {
        Transaction transaction = null;
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            transaction = s.beginTransaction();

            if (patient.getUser() == null) {
                throw new RuntimeException("User information is required for a Patient");
            }

            // Kiểm tra username đã tồn tại hay chưa
            if (isUsernameExists(s, patient.getUser().getUsername())) {
                throw new RuntimeException("Username '" + patient.getUser().getUsername() + "' already exists");
            }

            if (patient.getUser().getId() == 0) {
                s.persist(patient.getUser());
                s.flush(); // Đảm bảo User được lưu và có ID
            }

            s.persist(patient);
            transaction.commit();
            return patient;
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to add patient: " + e.getMessage(), e);
        }
    }

    // Cập nhật thông tin bệnh nhân
    public Patient updatePatient(Patient patient) {
        Transaction transaction = null;
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            transaction = s.beginTransaction();

            Patient existingPatient = s.get(Patient.class, patient.getId());
            if (existingPatient == null) {
                throw new RuntimeException("Patient with ID " + patient.getId() + " not found");
            }

            if (patient.getUser() == null) {
                throw new RuntimeException("User information is required for a Patient");
            }

            if (patient.getUser().getId() != patient.getId()) {
                throw new RuntimeException("User ID must match Patient ID due to @MapsId mapping");
            }

            // Kiểm tra username nếu có thay đổi
            User existingUser = s.get(User.class, patient.getUser().getId());
            if (!existingUser.getUsername().equals(patient.getUser().getUsername()) && isUsernameExists(s, patient.getUser().getUsername())) {
                throw new RuntimeException("Username '" + patient.getUser().getUsername() + "' already exists");
            }

            s.merge(patient.getUser());
            s.flush();

            Patient updatedPatient = (Patient) s.merge(patient);
            transaction.commit();
            return updatedPatient;
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    // Xóa bệnh nhân
    public void deletePatient(Integer id) {
        Transaction transaction = null;
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            transaction = s.beginTransaction();

            Patient patient = s.get(Patient.class, id);
            if (patient == null) {
                throw new RuntimeException("Patient with ID " + id + " not found");
            }

            if (patient.getUser() != null) {
                s.remove(patient.getUser());
            }

            s.remove(patient);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete patient: " + e.getMessage(), e);
        }
    }

}
