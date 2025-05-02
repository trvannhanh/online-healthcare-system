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

    //Lọc theo ngày thanh toán 
    List<Payment> getPaymentByPaymentDate(String createAt);
}
