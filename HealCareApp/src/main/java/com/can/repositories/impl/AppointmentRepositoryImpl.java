/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.repositories.AppointmentRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
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
public class AppointmentRepositoryImpl implements AppointmentRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public Appointment addAppointment(Appointment appointment) {
        Session s = this.factory.getObject().getCurrentSession();
        Doctor doctor = s.get(Doctor.class, appointment.getDoctor().getId());

        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<Appointment> root = q.from(Appointment.class);
        q.select(b.count(root));
        q.where(
                b.equal(root.get("doctor"), doctor),
                b.equal(root.get("appointmentDate"),
                        new java.sql.Timestamp(appointment.getAppointmentDate().getTime())),
                b.notEqual(root.get("status"), AppointmentStatus.CANCELLED));
        Long count = s.createQuery(q).getSingleResult();
        if (count > 0) {
            throw new RuntimeException("Bác sĩ đã có lịch hẹn vào thời gian này");
        }

        s.persist(appointment);
        return appointment;
    }

    @Override
    public Appointment cancelAppointment(int id) {
        Session s = this.factory.getObject().getCurrentSession();

        Appointment existingAppointment = s.get(Appointment.class, id);
        if (existingAppointment == null) {
            throw new RuntimeException("Lịch Hẹn với id " + id + " không tìm thấy");
        }

        existingAppointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updatedAppointment = (Appointment) s.merge(existingAppointment);

        return updatedAppointment;
    }

    @Override
    public Appointment rescheduleAppointment(int id, Date newDate) {
        Session s = this.factory.getObject().getCurrentSession();

        Appointment existingAppointment = s.get(Appointment.class, id);
        if (existingAppointment == null) {
            throw new RuntimeException("Lịch Hẹn với id " + id + " không tìm thấy");
        }

        Doctor doctor = existingAppointment.getDoctor();
        if (doctor != null) {
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Long> q = b.createQuery(Long.class);
            Root<Appointment> root = q.from(Appointment.class);
            q.select(b.count(root));
            q.where(
                    b.equal(root.get("doctor"), doctor),
                    b.equal(root.get("appointmentDate"), newDate),
                    b.notEqual(root.get("status"), AppointmentStatus.CANCELLED),
                    b.notEqual(root.get("id"), id));
            Long count = s.createQuery(q).getSingleResult();
            if (count > 0) {
                throw new RuntimeException("Bác sĩ đã có lịch vào thời gian này");
            }
        }

        existingAppointment.setAppointmentDate(newDate);
        Appointment updatedAppointment = (Appointment) s.merge(existingAppointment);

        return updatedAppointment;
    }

    @Override
    public List<Appointment> getAppointments(Map<String, String> params) throws ParseException {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);

        Join<Appointment, Doctor> doctorJoin = root.join("doctor");
        Join<Doctor, User> doctorUserJoin = doctorJoin.join("user");
        Join<Appointment, Patient> patientJoin = root.join("patient");
        Join<Patient, User> patientUserJoin = patientJoin.join("user");

        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(b.equal(root.get("status"), AppointmentStatus.valueOf(status.toUpperCase())));
            }

            String appointmentDate = params.get("appointmentDate");
            if (appointmentDate != null && !appointmentDate.isEmpty()) {
                Date date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(appointmentDate);
                predicates.add(b.equal(b.function("DATE", Date.class, root.get("appointmentDate")), date));
            }

            String doctorId = params.get("doctorId");
            if (doctorId != null && !doctorId.isEmpty()) {
                predicates.add(b.equal(doctorJoin.get("id"), Long.parseLong(doctorId)));
            }

            String fromDateStr = params.get("fromDate");
            String toDateStr = params.get("toDate");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (fromDateStr != null && toDateStr != null) {
                Date fromDate = sdf.parse(fromDateStr);
                Date toDate = sdf.parse(toDateStr);
                predicates.add(b.between(root.get("appointmentDate"), fromDate, toDate));
            }

            String doctorName = params.get("doctorName");
            if (doctorName != null && !doctorName.isEmpty()) {
                String searchPattern = String.format("%%%s%%", doctorName.toLowerCase());
                Predicate firstNamePredicate = b.like(
                        b.lower(doctorUserJoin.get("firstName")),
                        searchPattern);
                Predicate lastNamePredicate = b.like(
                        b.lower(doctorUserJoin.get("lastName")),
                        searchPattern);
                predicates.add(b.or(firstNamePredicate, lastNamePredicate));

            }

            String patientName = params.get("patientName");
            if (patientName != null && !patientName.isEmpty()) {
                String searchPattern = String.format("%%%s%%", patientName.toLowerCase());
                Predicate firstNamePredicate = b.like(
                        b.lower(patientUserJoin.get("firstName")),
                        searchPattern);
                Predicate lastNamePredicate = b.like(
                        b.lower(patientUserJoin.get("lastName")),
                        searchPattern);
                predicates.add(b.or(firstNamePredicate, lastNamePredicate));
            }
        }

        if (!predicates.isEmpty()) {
            q.where(predicates.toArray(Predicate[]::new));
        }

        q.orderBy(b.asc(root.get("appointmentDate")));

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
    public List<Appointment> getAllAppointments() throws ParseException {
        return getAppointments(null);
    }

    @Override
    public Appointment getAppointmentById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);

        root.fetch("doctor").fetch("user");
        root.fetch("patient").fetch("user");

        q.where(b.equal(root.get("id"), id));

        Query query = s.createQuery(q);
        return (Appointment) query.getSingleResult();

    }

    @Override
    public Appointment updateAppointment(Appointment appointment) {
        Session s = this.factory.getObject().getCurrentSession();

        Appointment existingAppointment = s.get(Appointment.class, appointment.getId());
        if (existingAppointment == null) {
            throw new RuntimeException("Appointment with ID " + appointment.getId() + " not found");
        }

        if (appointment.getPatient() == null || appointment.getDoctor() == null) {
            throw new RuntimeException("Patient and Doctor are required for an Appointment");
        }

        Patient patient = s.get(Patient.class, appointment.getPatient().getId());
        Doctor doctor = s.get(Doctor.class, appointment.getDoctor().getId());
        if (patient == null) {
            throw new RuntimeException("Patient with ID " + appointment.getPatient().getId() + " not found");
        }
        if (doctor == null) {
            throw new RuntimeException("Doctor with ID " + appointment.getDoctor().getId() + " not found");
        }

        boolean dateChanged = (existingAppointment.getAppointmentDate() == null
                && appointment.getAppointmentDate() != null)
                || (existingAppointment.getAppointmentDate() != null && appointment.getAppointmentDate() != null
                && !existingAppointment.getAppointmentDate().equals(appointment.getAppointmentDate()));

        boolean doctorChanged = false;
        if (existingAppointment.getDoctor() == null && appointment.getDoctor() != null) {
            doctorChanged = true;
        } else if (existingAppointment.getDoctor() != null && appointment.getDoctor() == null) {
            doctorChanged = true;
        } else if (existingAppointment.getDoctor() != null && appointment.getDoctor() != null) {
            Integer existingDoctorId = existingAppointment.getDoctor().getId();
            Integer newDoctorId = appointment.getDoctor().getId();
            doctorChanged = (existingDoctorId == null && newDoctorId != null)
                    || (existingDoctorId != null && newDoctorId == null)
                    || (existingDoctorId != null && newDoctorId != null && !existingDoctorId.equals(newDoctorId));
        }

        if (dateChanged || doctorChanged) {
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Long> q = b.createQuery(Long.class);
            Root<Appointment> root = q.from(Appointment.class);
            q.select(b.count(root));
            q.where(
                    b.equal(root.get("doctor"), doctor),
                    b.equal(root.get("appointmentDate"), appointment.getAppointmentDate()),
                    b.notEqual(root.get("status"), AppointmentStatus.CANCELLED),
                    b.notEqual(root.get("id"), appointment.getId()));
            Long count = s.createQuery(q).getSingleResult();
            if (count > 0) {
                throw new RuntimeException("Doctor is already booked at this time");
            }
        }

        Appointment updatedAppointment = (Appointment) s.merge(appointment);
        return updatedAppointment;
    }

    @Override
    public void deleteAppointment(int id) {
        Session session = this.factory.getObject().getCurrentSession();

        Appointment appointment = session.get(Appointment.class, id);
        if (appointment == null) {
            throw new RuntimeException("Appointment with ID " + id + " not found");
        }

        session.remove(appointment);
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status, int page) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);

        root.fetch("doctor").fetch("user");
        root.fetch("patient").fetch("user");

        q.where(b.equal(root.get("status"), status));
        q.orderBy(b.asc(root.get("appointmentDate")));

        Query query = s.createQuery(q);
        int start = (page - 1) * PAGE_SIZE;
        query.setFirstResult(start);
        query.setMaxResults(PAGE_SIZE);

        return query.getResultList();
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(int doctorId, int page) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);

        root.fetch("doctor").fetch("user");
        root.fetch("patient").fetch("user");

        q.where(b.equal(root.get("doctor").get("id"), doctorId));
        q.orderBy(b.asc(root.get("appointmentDate")));

        Query query = s.createQuery(q);
        int start = (page - 1) * PAGE_SIZE;
        query.setFirstResult(start);
        query.setMaxResults(PAGE_SIZE);

        return query.getResultList();

    }

    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId, int page) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);

        root.fetch("doctor").fetch("user");
        root.fetch("patient").fetch("user");

        q.where(b.equal(root.get("patient").get("id"), patientId));
        q.orderBy(b.asc(root.get("appointmentDate")));

        Query query = s.createQuery(q);
        int start = (page - 1) * PAGE_SIZE;
        query.setFirstResult(start);
        query.setMaxResults(PAGE_SIZE);

        return query.getResultList();

    }

    @Override
    public Appointment confirmAppointment(int id) {
        Session s = this.factory.getObject().getCurrentSession();

        Appointment existingAppointment = s.get(Appointment.class, id);
        if (existingAppointment == null) {
            throw new RuntimeException("Appointment with ID " + id + " not found");
        }

        existingAppointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment updatedAppointment = (Appointment) s.merge(existingAppointment);

        return updatedAppointment;
    }

    @Override
    public List<Appointment> getAppointmentsWithFilters(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);

        root.fetch("doctor").fetch("user");
        root.fetch("patient").fetch("user");

        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(b.equal(root.get("status"), AppointmentStatus.valueOf(status.toUpperCase())));
            }

            String doctorId = params.get("doctorId");
            if (doctorId != null && !doctorId.isEmpty()) {
                predicates.add(b.equal(root.get("doctor").get("id"), Integer.parseInt(doctorId)));
            }

            String patientId = params.get("patientId");
            if (patientId != null && !patientId.isEmpty()) {
                predicates.add(b.equal(root.get("patient").get("id"), Integer.parseInt(patientId)));
            }
        }

        if (!predicates.isEmpty()) {
            q.where(predicates.toArray(Predicate[]::new));
        }

        q.orderBy(b.asc(root.get("appointmentDate")));

        Query query = s.createQuery(q);

        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;
            query.setFirstResult(start);
            query.setMaxResults(PAGE_SIZE);
        }

        return query.getResultList();
    }
}
