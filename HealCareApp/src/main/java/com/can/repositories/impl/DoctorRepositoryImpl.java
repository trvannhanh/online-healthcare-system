/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.Doctor;
import com.can.pojo.User;
import com.can.repositories.DoctorRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
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
public class DoctorRepositoryImpl implements DoctorRepository{

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Doctor> q = b.createQuery(Doctor.class);
        Root<Doctor> root = q.from(Doctor.class);

        Join<Doctor, User> userJoin = root.join("user");

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            //Lọc theo tên bác sĩ
            String doctorName = params.get("doctorName");
            if (doctorName != null && !doctorName.isEmpty()) {
                String searchPattern = String.format("%%%s%%", doctorName.toLowerCase());
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

            //Lọc theo chuyên khoa
            String specialization = params.get("specialization");
            if (specialization != null && !specialization.isEmpty()) {
                predicates.add(b.equal(
                        b.lower(root.get("specialization")),
                        specialization.toLowerCase()
                ));
            }

            //Lọc theo bệnh viện
            String hospital = params.get("hospital");
            if (hospital != null && !hospital.isEmpty()) {
                predicates.add(b.equal(
                        b.lower(root.get("hospital")),
                        hospital.toLowerCase()
                ));
            }

            // Áp dụng các điều kiện lọc
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
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();
        transaction = s.beginTransaction();

        if (doctor.getUser() == null) {
            throw new RuntimeException("Thông tin user cần thiết cho một Bác sĩ");
        }

        if (doctor.getUser().getId() == 0) {
            s.persist(doctor.getUser());
            s.flush();
        }

        doctor.setId(doctor.getUser().getId());

        s.persist(doctor);
        transaction.commit();
        return doctor;

    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();
        transaction = s.beginTransaction();

        Doctor existingDoctor = s.get(Doctor.class, doctor.getId());
        if (existingDoctor == null) {
            throw new RuntimeException("Doctor with ID " + doctor.getId() + "not found");
        }

        if (doctor.getUser() == null) {
            throw new RuntimeException("Thông tin user cần thiết cho một Bác sĩ");
        }

        if (doctor.getUser().getId() != doctor.getId()) {
            throw new RuntimeException("Id User và Id bác sĩ không trùng khớp");
        }

        s.merge(doctor.getUser());
        s.flush();

        Doctor updatedDoctor = (Doctor) s.merge(doctor);
        transaction.commit();
        return updatedDoctor;

    }

    @Override
    public void deleteDoctor(int id) {
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();
        transaction = s.beginTransaction();

        Doctor doctor = s.get(Doctor.class, id);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID" + id + "not found");
        }

        if (doctor.getUser() != null) {
            s.remove(doctor.getUser());
        }

        s.remove(doctor);
        transaction.commit();

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
        Transaction transaction = null;
        Session s = this.factory.getObject().getCurrentSession();
        transaction = s.beginTransaction();

        Doctor doctor = s.get(Doctor.class, doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID" + doctorId + "not found");
        }

        if (doctor.isIsVerified()) {
            throw new RuntimeException("Doctor with ID" + doctorId + " đã được chứng nhận rồi ");
        }

        if (doctor.getLicenseNumber() == null || doctor.getLicenseNumber().trim().isEmpty()) {
            throw new RuntimeException("Doctor with ID" + doctorId + "chưa cấp giấy phép");
        }

        doctor.setIsVerified(true);
        s.merge(doctor);
        transaction.commit();

    }

    @Override
    public boolean isDoctorVerified(int doctorId) {
        Session s = this.factory.getObject().getCurrentSession();
        Doctor doctor = s.get(Doctor.class,
                doctorId);
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID" + doctorId + "not found");
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
            throw new RuntimeException("Doctor with ID" + doctorId + "not found");
        }

        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new RuntimeException("Chưa nhập giấy phép hành nghề kìa");
        }

        doctor.setLicenseNumber(licenseNumber);
        s.merge(doctor);
        transaction.commit();

    }

}
