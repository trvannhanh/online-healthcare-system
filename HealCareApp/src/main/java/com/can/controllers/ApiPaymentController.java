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
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiPaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/secure/payment/{id}/create")
    public ResponseEntity<?> createPayment(@PathVariable("id") int appointmentId, @RequestParam(value = "amount") double amount, Principal principal) 
    {
        try {
            Payment payment = paymentService.createPaymentForAppointment(appointmentId, amount, principal.getName());
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/secure/payment/{id}/process")
    public ResponseEntity<?> processPayment(
            @PathVariable("id") int paymentId,
            @RequestParam(value = "paymentMethod") PaymentMethod paymentMethod,
            Principal principal) {
        try {
            String paymentUrl = paymentService.processPayment(paymentId, paymentMethod, principal);
            return new ResponseEntity<>(paymentUrl, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment/return")
    public ResponseEntity<?> handlePaymentReturn(@RequestParam Map<String, String> params) {
        try {
            String paymentMethod = params.containsKey("resultCode") ? "Momo" : "VNPay";
            paymentService.handlePaymentCallback(paymentMethod, params);
            String transactionId = params.get("orderId");
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESSFUL) {
                return new ResponseEntity<>("Thanh toán thành công!", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Thanh toán thất bại!", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/payment/notify")
    public ResponseEntity<?> handlePaymentNotify(@RequestParam Map<String, String> params) {
        try {
            String paymentMethod = params.containsKey("resultCode") ? "MOMO" : "VNPAY";
            paymentService.handlePaymentCallback(paymentMethod, params);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment")
    public ResponseEntity<List<Payment>> getPaymentsByCriteria(@RequestParam Map<String, String> params) {
        try {
            List<Payment> payments = paymentService.getPaymentsByCriteria(params);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Integer paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/payment/appointment/{appointmentId}")
    public ResponseEntity<Payment> getPaymentByAppointmentId(@PathVariable("appointmentId") Integer appointmentId) {
        Payment payment = paymentService.getPaymentByAppointment_Id(appointmentId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/payment/filter/status")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@RequestParam PaymentStatus status) {
        try {
            List<Payment> payments = paymentService.getPaymentByPaymentStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payment/filter/method")
    public ResponseEntity<List<Payment>> getPaymentsByMethod(@RequestParam PaymentMethod method) {
        try {
            List<Payment> payments = paymentService.findByPaymentMethod(method);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payment/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/payment/filter/amount-greater")
    public ResponseEntity<List<Payment>> getPaymentsByAmountGreater(@RequestParam double amount) {
        try {
            List<Payment> payments = paymentService.getPaymentByAmountGreaterThan(amount);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payment/filter/amount-less")
    public ResponseEntity<List<Payment>> getPaymentsByAmountLess(@RequestParam double amount) {
        try {
            List<Payment> payments = paymentService.getPaymentByAmountLessThan(amount);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/payment/filter/date")
    public ResponseEntity<List<Payment>> getPaymentsByPaymentDate(@RequestParam String createAt) {
        try {
            List<Payment> payments = paymentService.getPaymentByPaymentDate(createAt);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
