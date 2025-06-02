package com.can.repositories;

import com.can.pojo.Payment;
import com.can.pojo.PaymentMethod;
import com.can.pojo.PaymentStatus;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
public interface PaymentRepository {
    List<Payment> getPaymentsByCriteria(Map<String, String> params);

    Payment getPaymentById(Integer id);
    
    Payment getPaymentByAppointment_Id(Integer appointmentId);
    
    List<Payment> getPaymentByPaymentStatus(PaymentStatus paymentStatus);
    
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    Payment getPaymentByTransactionId(String transactionId);
    
    List<Payment> getPaymentByAmountGreaterThan(double amount);
    
    List<Payment> getPaymentByAmountLessThan(double amount);

    List<Payment> getPaymentByPaymentDate(String createAt);
    
    Payment createPaymentForAppointment(int appointmentId, double amount);
    
    void updatePayment(Payment payment);
    
}
