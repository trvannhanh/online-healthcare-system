package com.can.services.impl;

import com.can.pojo.Payment;

import com.can.pojo.PaymentMethod;
import com.can.pojo.PaymentStatus;
import com.can.repositories.PaymentRepository;
import com.can.services.PaymentService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<Payment> getPaymentsByCriteria(java.util.Map<String, String> params) {
        return this.paymentRepository.getPaymentsByCriteria(params);
    }

    @Override
    public Payment getPaymentById(Integer id) {
        return this.paymentRepository.getPaymentById(id);
    }

    @Override
    public Payment getPaymentByAppointment_Id(Integer appointmentId) {
        return this.paymentRepository.getPaymentByAppointment_Id(appointmentId);
    }

    @Override
    public List<Payment> getPaymentByPaymentStatus(PaymentStatus paymentStatus) {
        return this.paymentRepository.getPaymentByPaymentStatus(paymentStatus);
    }

    @Override
    public List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) {
        return this.paymentRepository.findByPaymentMethod(paymentMethod);
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return this.paymentRepository.getPaymentByTransactionId(transactionId);
    }

    @Override
    public List<Payment> getPaymentByAmountGreaterThan(double amount) {
        return this.paymentRepository.getPaymentByAmountGreaterThan(amount);
    }

    @Override
    public List<Payment> getPaymentByAmountLessThan(double amount) {
        return this.paymentRepository.getPaymentByAmountLessThan(amount);
    }

    @Override
    public List<Payment> getPaymentByPaymentDate(String createAt) {
        return this.paymentRepository.getPaymentByPaymentDate(createAt);
    }
}
