/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.Doctor;
import com.can.pojo.Hospital;
import com.can.pojo.Specialization;
import com.can.pojo.User;
import com.can.pojo.VerificationStatus;
import com.can.repositories.DoctorRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
public class DoctorRepositoryImpl implements DoctorRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 4;

    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Doctor> q = b.createQuery(Doctor.class);
        Root<Doctor> root = q.from(Doctor.class);

        Join<Doctor, User> userJoin = root.join("user");

        Join<Doctor, Specialization> specializationJoin = root.join("specialization", JoinType.LEFT);
        Join<Doctor, Hospital> hospitalJoin = root.join("hospital", JoinType.LEFT);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            String doctorName = params.get("doctorName");
            if (doctorName != null && !doctorName.isEmpty()) {
                // Splits doctor name into parts and builds predicates for flexible name search
                String[] nameParts = doctorName.trim().toLowerCase().split("\\s+");

                List<Predicate> namePredicates = new ArrayList<>();

                for (String part : nameParts) {
                    String pattern = "%" + part + "%";
                    namePredicates.add(b.or(
                            b.like(b.lower(userJoin.get("firstName")), pattern),
                            b.like(b.lower(userJoin.get("lastName")), pattern)
                    ));
                }

                predicates.add(b.and(namePredicates.toArray(Predicate[]::new)));
            }

            String specialization = params.get("specialization");
            if (specialization != null && !specialization.isEmpty()) {
                predicates.add(b.equal(
                        b.lower(specializationJoin.get("name")),
                        specialization.toLowerCase()
                ));
            }


            String hospital = params.get("hospital");
            if (hospital != null && !hospital.isEmpty()) {
                predicates.add(b.equal(
                        b.lower(hospitalJoin.get("name")),
                        hospital.toLowerCase()
                ));
            }

            q.where(predicates.toArray(Predicate[]::new));
        }

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
    public Doctor getDoctorById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Doctor> q = b.createQuery(Doctor.class
        );
        Root<Doctor> root = q.from(Doctor.class
        );

        Join<Doctor, User> userJoin = root.join("user");

        q.where(b.equal(userJoin.get("id"), id));

        Query query = s.createQuery(q);
        return (Doctor) query.getSingleResult();
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return getDoctors(null);
    }

    @Override
    public Doctor addDoctor(Doctor doctor) {
        Session s = this.factory.getObject().getCurrentSession();

        if (doctor.getUser() == null) {
            throw new RuntimeException("User information is required for a Doctor");
        }

        if (doctor.getUser().getId() == 0) {
            s.persist(doctor.getUser());
            s.flush();
        }

        doctor.setId(doctor.getUser().getId());

        s.persist(doctor);
        return doctor;

    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            Doctor existingDoctor = s.get(Doctor.class, doctor.getId());
            if (existingDoctor == null) {
                throw new RuntimeException("Doctor with ID " + doctor.getId() + " not found");
            }
            if (doctor.getUser() == null) {
                throw new RuntimeException("User information is required for a Doctor");
            }
            if (doctor.getUser().getId() != doctor.getId()) {
                throw new RuntimeException("User ID and Doctor ID do not match");
            }
            if (doctor.getHospital() == null || doctor.getSpecialization() == null) {
                throw new RuntimeException("Hospital and Specialization are required");
            }
            // Retains existing verification status if not provided
            if (doctor.getVerificationStatus() == null) {
                doctor.setVerificationStatus(existingDoctor.getVerificationStatus());
            }
            s.merge(doctor.getUser());
            Doctor updatedDoctor = (Doctor) s.merge(doctor);
            s.flush();
            return updatedDoctor;
        } catch (Exception e) {
            throw new RuntimeException("Error updating doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDoctor(int id) {
        Session s = this.factory.getObject().getCurrentSession();

        Doctor doctor = s.get(Doctor.class, id);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID" + id + " not found");
        }

        if (doctor.getUser() != null) {
            s.remove(doctor.getUser());
        }

        s.remove(doctor);

    }

    @Override
    public List<Doctor> getDoctorByVerificationStatus(boolean isVerified, int page) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Doctor> q = b.createQuery(Doctor.class
        );
        Root<Doctor> root = q.from(Doctor.class
        );

        Join<Doctor, User> userJoin = root.join("user");

        q.where(b.equal(userJoin.get("isVerified"), isVerified));
        q.orderBy(b.asc(userJoin.get("id")));

        Query query = s.createQuery(q);
        int start = (page - 1) * PAGE_SIZE;
        query.setFirstResult(start);
        query.setMaxResults(PAGE_SIZE);

        return query.getResultList();
    }

    @Override
    public void verifyDoctor(int doctorId) {
        Session s = this.factory.getObject().getCurrentSession();

        Doctor doctor = s.get(Doctor.class, doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID " + doctorId + " not found");
        }

        if (doctor.isIsVerified()) {
            throw new RuntimeException("Doctor with ID " + doctorId + " is already verified");
        }

        if (doctor.getLicenseNumber() == null || doctor.getLicenseNumber().trim().isEmpty()) {
            throw new RuntimeException("Doctor with ID " + doctorId + " has not provided a license number");
        }

        // Updates both isVerified and verificationStatus
        doctor.setIsVerified(true);
        doctor.setVerificationStatus(VerificationStatus.APPROVED); // Cập nhật thành Approved

        s.merge(doctor);
    }

    @Override
    public boolean isDoctorVerified(int doctorId) {
        Session s = this.factory.getObject().getCurrentSession();
        Doctor doctor = s.get(Doctor.class,
                doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID" + doctorId + " not found");
        }

        return doctor.isIsVerified();
    }

    @Override
    public List<Doctor> getUnverifiedDoctors(int page) {
        return getDoctorByVerificationStatus(false, page);
    }

    @Override
    public void updateLicenseNumber(int doctorId, String licenseNumber) {
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();
        transaction = s.beginTransaction();

        Doctor doctor = s.get(Doctor.class, doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID" + doctorId + " not found");
        }

        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new RuntimeException("License number is required");
        }

        doctor.setLicenseNumber(licenseNumber);
        s.merge(doctor);
        transaction.commit();

    }

}