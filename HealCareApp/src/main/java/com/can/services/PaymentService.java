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

/**
 *
 * @author Giidavibe
 */
public interface PaymentService {
    // Lọc Payment theo các tiêu chí động (sử dụng Map)
    List<Payment> getPaymentsByCriteria(Map<String, String> params);

    // Tìm Payment theo id
    Payment getPaymentById(Integer id);
    
    // Tìm Payment theo lịch hẹn
    Payment getPaymentByAppointment_Id(Integer appointmentId);
    
    // Lọc Payment theo trạng thái thanh toán
    List<Payment> getPaymentByPaymentStatus(PaymentStatus paymentStatus);
    
    // Lọc Payment theo phương thức thanh toán
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    // Lọc Payment theo transactionId
    Payment getPaymentByTransactionId(String transactionId);
    
    // Lọc Payment theo amount lớn hơn một giá trị
    List<Payment> getPaymentByAmountGreaterThan(double amount);
    
    // Lọc Payment theo amount nhỏ hơn một giá trị
    List<Payment> getPaymentByAmountLessThan(double amount);

    // Lọc Payment theo ngày thanh toán
    List<Payment> getPaymentByPaymentDate(String createAt);
}
