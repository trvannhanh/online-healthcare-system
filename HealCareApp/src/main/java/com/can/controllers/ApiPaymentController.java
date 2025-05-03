package com.can.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.can.pojo.Payment;
import com.can.pojo.PaymentMethod;
import com.can.pojo.PaymentStatus;
import com.can.services.PaymentService;

import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/payment")
public class ApiPaymentController {

    @Autowired
    private PaymentService paymentService;

    // Lấy danh sách Payment theo tiêu chí động
    @GetMapping
    public ResponseEntity<List<Payment>> getPaymentsByCriteria(@RequestParam Map<String, String> params) {
        try {
            List<Payment> payments = paymentService.getPaymentsByCriteria(params);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy Payment theo id
    @GetMapping("{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    // Lấy Payment theo appointmentId
    @GetMapping("appointment/{appointmentId}")
    public ResponseEntity<Payment> getPaymentByAppointmentId(@PathVariable Integer appointmentId) {
        Payment payment = paymentService.getPaymentByAppointment_Id(appointmentId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    // Lọc Payment theo trạng thái thanh toán
    @GetMapping("/filter/status")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@RequestParam PaymentStatus status) {
        try {
            List<Payment> payments = paymentService.getPaymentByPaymentStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lọc Payment theo phương thức thanh toán
    @GetMapping("/filter/method")
    public ResponseEntity<List<Payment>> getPaymentsByMethod(@RequestParam PaymentMethod method) {
        try {
            List<Payment> payments = paymentService.findByPaymentMethod(method);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy Payment theo transactionId
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    // Lọc Payment theo amount lớn hơn
    @GetMapping("/filter/amount-greater")
    public ResponseEntity<List<Payment>> getPaymentsByAmountGreater(@RequestParam double amount) {
        try {
            List<Payment> payments = paymentService.getPaymentByAmountGreaterThan(amount);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lọc Payment theo amount nhỏ hơn
    @GetMapping("/filter/amount-less")
    public ResponseEntity<List<Payment>> getPaymentsByAmountLess(@RequestParam double amount) {
        try {
            List<Payment> payments = paymentService.getPaymentByAmountLessThan(amount);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lọc Payment theo ngày thanh toán (dùng chuỗi ngày)
    @GetMapping("/filter/date")
    public ResponseEntity<List<Payment>> getPaymentsByPaymentDate(@RequestParam String createAt) {
        try {
            List<Payment> payments = paymentService.getPaymentByPaymentDate(createAt);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
