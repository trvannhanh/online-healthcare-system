package com.can.repositories.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.can.pojo.Payment;
import com.can.pojo.PaymentMethod;
import com.can.pojo.PaymentStatus;
import com.can.repositories.AppointmentRepository;
import com.can.repositories.PaymentRepository;
import jakarta.persistence.NoResultException;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Date;
import org.hibernate.Session;

/**
 *
 * @author DELL
 */
@Repository
@Transactional
public class PaymentRepositoryImpl implements PaymentRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private AppointmentRepository appRepo;

    private static final int PAGE_SIZE = 10;

    @Override
    public Payment getPaymentById(Integer id) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.equal(root.get("id"), id));
        Query q = session.createQuery(query);
        return (Payment) q.getSingleResult();
    }

    @Override
    public Payment getPaymentByAppointment_Id(Integer appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.equal(root.get("appointment").get("id"), appointmentId));
        Query q = session.createQuery(query);

        try {
            return (Payment) q.getSingleResult();
        } catch (NoResultException e) {
            return null; // Trả về null nếu không tìm thấy Payment
        }
    }

    @Override
    public List<Payment> getPaymentsByCriteria(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = criteriaBuilder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        List<Predicate> predicates = new ArrayList<>();

        // Kiểm tra các tham số lọc trong Map
        if (params != null) {
            String paymentMethod = params.get("paymentMethod");
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), PaymentMethod.valueOf(paymentMethod)));
            }

            String paymentStatus = params.get("paymentStatus");
            if (paymentStatus != null && !paymentStatus.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), PaymentStatus.valueOf(paymentStatus)));
            }

            String minAmount = params.get("minAmount");
            if (minAmount != null && !minAmount.isEmpty()) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), Double.parseDouble(minAmount)));
            }

            String maxAmount = params.get("maxAmount");
            if (maxAmount != null && !maxAmount.isEmpty()) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), Double.parseDouble(maxAmount)));
            }

            String transactionId = params.get("transactionId");
            if (transactionId != null && !transactionId.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("transactionId"), transactionId));
            }

            String appointmentId = params.get("appointmentId");
            if (appointmentId != null && !appointmentId.isEmpty()) {
                predicates
                        .add(criteriaBuilder.equal(root.get("appointment").get("id"), Integer.parseInt(appointmentId)));
            }

            // Lọc theo ngày tạo (createdAt)
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");

            if (startDate != null && !startDate.isEmpty()) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), java.sql.Date.valueOf(startDate)));
            }

            if (endDate != null && !endDate.isEmpty()) {
                predicates
                        .add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), java.sql.Date.valueOf(endDate)));
            }

        }

        // Nếu có predicate, thêm vào điều kiện where
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        // Thực thi truy vấn và trả về kết quả
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Payment> getPaymentByPaymentStatus(PaymentStatus paymentStatus) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.equal(root.get("paymentStatus"), paymentStatus));
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.equal(root.get("paymentMethod"), paymentMethod));
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.equal(root.get("transactionId"), transactionId));
        Query q = session.createQuery(query);
        return (Payment) q.getSingleResult();
    }

    @Override
    public List<Payment> getPaymentByAmountGreaterThan(double amount) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.greaterThan(root.get("amount"), amount));
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Payment> getPaymentByAmountLessThan(double amount) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.lessThan(root.get("amount"), amount));
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Payment> getPaymentByPaymentDate(String createAt) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Payment> query = builder.createQuery(Payment.class);
        Root<Payment> root = query.from(Payment.class);

        query.where(builder.equal(root.get("createAt"), createAt));
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public Payment createPaymentForAppointment(int appointmentId, double amount) {
        Session session = this.factory.getObject().getCurrentSession();
        Appointment appointment = appRepo.getAppointmentById(appointmentId);
        if (appointment == null) {
            System.out.println("Lịch hẹn không tồn tại");
//            throw new ResourceNotFoundException("Lịch hẹn không tồn tại: " + appointmentId);
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Lịch hẹn chưa hoàn thành, không thể tạo thanh toán");
        }

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreateAt(new Date());
        session.save(payment);
        return payment;
    }

    @Override
    public void updatePayment(Payment payment) {
        Session session = this.factory.getObject().getCurrentSession();
        session.update(payment);
    }
}
