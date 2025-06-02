/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import java.util.List;
import java.util.Map;

import com.can.pojo.Payment;
import com.can.pojo.PaymentMethod;
import com.can.pojo.PaymentStatus;
import java.security.Principal;

/**
 *
 * @author DELL
 */
public interface PaymentService {
    List<Payment> getPaymentsByCriteria(Map<String, String> params);

    Payment getPaymentById(Integer id);
    
    Payment getPaymentByAppointment_Id(Integer appointmentId);
    
    List<Payment> getPaymentByPaymentStatus(PaymentStatus paymentStatus);
    
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    Payment getPaymentByTransactionId(String transactionId);
    
    List<Payment> getPaymentByAmountGreaterThan(double amount);
    
    List<Payment> getPaymentByAmountLessThan(double amount);

    List<Payment> getPaymentByPaymentDate(String createAt);
    
    Payment createPaymentForAppointment(int appointmentId, double amount, String username);
    
    String processPayment(int paymentId, PaymentMethod paymentMethod, Principal principal) throws Exception;
    
    void handlePaymentCallback(String paymentMethod, Map<String, String> params);
    
    
}
