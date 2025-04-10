/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repository.impl;

import com.can.healcarehibernate.HibernateUtils;
import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.Patient;
import com.can.pojo.User;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

/**
 *
 * @author Giidavibe
 */
public class AppointmentRepositoryImpl {

    private static final int PAGE_SIZE = 10;

    // Lấy danh sách lịch hẹn với bộ lọc
    public List<Appointment> getAppointments(Map<String, String> params) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
            Root<Appointment> root = q.from(Appointment.class);

            // Fetch Doctor và Patient để lấy thông tin ngay lập tức
            root.fetch("doctor").fetch("user");
            root.fetch("patient").fetch("user");

            Join<Appointment, Doctor> doctorJoin = root.join("doctor");
            Join<Doctor, User> doctorUserJoin = doctorJoin.join("user");
            Join<Appointment, Patient> patientJoin = root.join("patient");
            Join<Patient, User> patientUserJoin = patientJoin.join("user");

            List<Predicate> predicates = new ArrayList<>();

            if (params != null) {
                // Lọc theo trạng thái
                String status = params.get("status");
                if (status != null && !status.isEmpty()) {
                    predicates.add(b.equal(root.get("status"), AppointmentStatus.valueOf(status.toUpperCase())));
                }

                // Lọc theo ngày hẹn
                String appointmentDate = params.get("appointmentDate");
                if (appointmentDate != null && !appointmentDate.isEmpty()) {
                    // Giả sử appointmentDate được truyền vào dạng "yyyy-MM-dd"
                    Date date = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(appointmentDate);
                    predicates.add(b.equal(b.function("DATE", Date.class, root.get("appointmentDate")), date));
                }

                // Lọc theo tên bác sĩ
                String doctorName = params.get("doctorName");
                if (doctorName != null && !doctorName.isEmpty()) {
                    String searchPattern = String.format("%%%s%%", doctorName.toLowerCase());
                    Predicate firstNamePredicate = b.like(
                            b.lower(doctorUserJoin.get("firstName")),
                            searchPattern
                    );
                    Predicate lastNamePredicate = b.like(
                            b.lower(doctorUserJoin.get("lastName")),
                            searchPattern
                    );
                    predicates.add(b.or(firstNamePredicate, lastNamePredicate));
                }

                // Lọc theo tên bệnh nhân
                String patientName = params.get("patientName");
                if (patientName != null && !patientName.isEmpty()) {
                    String searchPattern = String.format("%%%s%%", patientName.toLowerCase());
                    Predicate firstNamePredicate = b.like(
                            b.lower(patientUserJoin.get("firstName")),
                            searchPattern
                    );
                    Predicate lastNamePredicate = b.like(
                            b.lower(patientUserJoin.get("lastName")),
                            searchPattern
                    );
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to get appointments: " + e.getMessage(), e);
        }
    }

    // Lấy tất cả lịch hẹn
    public List<Appointment> getAllAppointments() {
        return getAppointments(null);
    }

    // Lấy lịch hẹn theo ID
    public Appointment getAppointmentById(int id) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
            Root<Appointment> root = q.from(Appointment.class);

            root.fetch("doctor").fetch("user");
            root.fetch("patient").fetch("user");

            q.where(b.equal(root.get("id"), id));

            Query query = s.createQuery(q);
            return (Appointment) query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get appointment by ID: " + e.getMessage(), e);
        }
    }

    // Thêm lịch hẹn mới
    public Appointment addAppointment(Appointment appointment) {
        Transaction transaction = null;
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            transaction = s.beginTransaction();

            if (appointment.getPatient() == null || appointment.getDoctor() == null) {
                throw new RuntimeException("Patient and Doctor are required for an Appointment");
            }

            // Kiểm tra xem bác sĩ và bệnh nhân có tồn tại không
            Patient patient = s.get(Patient.class, appointment.getPatient().getId());
            Doctor doctor = s.get(Doctor.class, appointment.getDoctor().getId());
            if (patient == null) {
                throw new RuntimeException("Patient with ID " + appointment.getPatient().getId() + " not found");
            }
            if (doctor == null) {
                throw new RuntimeException("Doctor with ID " + appointment.getDoctor().getId() + " not found");
            }

            // Kiểm tra xem bác sĩ có lịch hẹn trùng thời gian không
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Long> q = b.createQuery(Long.class);
            Root<Appointment> root = q.from(Appointment.class);
            q.select(b.count(root));
            q.where(
                    b.equal(root.get("doctor"), doctor),
                    b.equal(root.get("appointmentDate"), appointment.getAppointmentDate()),
                    b.notEqual(root.get("status"), AppointmentStatus.CANCELLED)
            );
            Long count = s.createQuery(q).getSingleResult();
            if (count > 0) {
                throw new RuntimeException("Doctor is already booked at this time");
            }

            s.persist(appointment);
            transaction.commit();
            return appointment;
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to add appointment: " + e.getMessage(), e);
        }
    }

    // Cập nhật lịch hẹn
    public Appointment updateAppointment(Appointment appointment) {
        Transaction transaction = null;
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            transaction = s.beginTransaction();

            Appointment existingAppointment = s.get(Appointment.class, appointment.getId());
            if (existingAppointment == null) {
                throw new RuntimeException("Appointment with ID " + appointment.getId() + " not found");
            }

            if (appointment.getPatient() == null || appointment.getDoctor() == null) {
                throw new RuntimeException("Patient and Doctor are required for an Appointment");
            }

            // Kiểm tra xem bác sĩ và bệnh nhân có tồn tại không
            Patient patient = s.get(Patient.class, appointment.getPatient().getId());
            Doctor doctor = s.get(Doctor.class, appointment.getDoctor().getId());
            if (patient == null) {
                throw new RuntimeException("Patient with ID " + appointment.getPatient().getId() + " not found");
            }
            if (doctor == null) {
                throw new RuntimeException("Doctor with ID " + appointment.getDoctor().getId() + " not found");
            }

            // Kiểm tra xem bác sĩ có lịch hẹn trùng thời gian không (nếu thay đổi thời gian hoặc bác sĩ)
            boolean dateChanged = (existingAppointment.getAppointmentDate() == null && appointment.getAppointmentDate() != null)
                    || (existingAppointment.getAppointmentDate() != null && appointment.getAppointmentDate() != null
                    && !existingAppointment.getAppointmentDate().equals(appointment.getAppointmentDate()));

            // Kiểm tra thay đổi bác sĩ
            boolean doctorChanged = false;
            if (existingAppointment.getDoctor() == null && appointment.getDoctor() != null) {
                doctorChanged = true;
            } else if (existingAppointment.getDoctor() != null && appointment.getDoctor() == null) {
                doctorChanged = true;
            } else if (existingAppointment.getDoctor() != null && appointment.getDoctor() != null) {
                Integer existingDoctorId = existingAppointment.getDoctor().getId();
                Integer newDoctorId = appointment.getDoctor().getId();
                // So sánh ID an toàn, kiểm tra null trước
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
                        b.notEqual(root.get("id"), appointment.getId())
                );
                Long count = s.createQuery(q).getSingleResult();
                if (count > 0) {
                    throw new RuntimeException("Doctor is already booked at this time");
                }
            }

            Appointment updatedAppointment = (Appointment) s.merge(appointment);
            transaction.commit();
            return updatedAppointment;
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    // Xóa lịch hẹn
    public void deleteAppointment(int id) {
        Transaction transaction = null;
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
            transaction = s.beginTransaction();

            Appointment appointment = s.get(Appointment.class, id);
            if (appointment == null) {
                throw new RuntimeException("Appointment with ID " + id + " not found");
            }

            s.remove(appointment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to delete appointment: " + e.getMessage(), e);
        }
    }

    // Lấy danh sách lịch hẹn theo trạng thái
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status, int page) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
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
    }

    // Lấy danh sách lịch hẹn của một bác sĩ
    public List<Appointment> getAppointmentsByDoctor(int doctorId, int page) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
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
    }

    // Lấy danh sách lịch hẹn của một bệnh nhân
    public List<Appointment> getAppointmentsByPatient(int patientId, int page) {
        try (Session s = HibernateUtils.getFACTORY().openSession()) {
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
    }

}
