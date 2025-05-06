package com.can.services.impl;

import com.can.configs.PaymentConfig;
import com.can.pojo.Appointment;
import com.can.pojo.Payment;

import com.can.pojo.PaymentMethod;
import com.can.pojo.PaymentStatus;
import com.can.pojo.User;
import com.can.repositories.AppointmentRepository;
import com.can.repositories.PaymentRepository;
import com.can.repositories.UserRepository;
import com.can.services.PaymentService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author DELL
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AppointmentRepository appRepo;
    
    @Autowired
    private UserRepository uServ;
    
    private final RestTemplate restTemplate = new RestTemplate();

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
    
    @Override
    public Payment createPaymentForAppointment(int appointmentId, double amount, String username){

        
        User u = this.uServ.getUserByUsername(username);
        String role = u.getRole().toString().toUpperCase();
        
        Appointment appointment = appRepo.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("Appointment not found");
        }
        
        if(!"DOCTOR".equalsIgnoreCase(role)){
            try {
                throw new AccessDeniedException("Chỉ bác sĩ mới có thể tạo thanh toán");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(!(u.getId() == appointment.getDoctor().getId()) ){
            try {
                throw new AccessDeniedException("Bạn không có quyền tạo thanh toán cho lịch hẹn này");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //Tạo Payment
        return paymentRepository.createPaymentForAppointment(appointmentId, amount);
        
        
    }
    
    // Bệnh nhân chọn phương thức thanh toán và nhận URL/mã thanh toán
    @Override
    public String processPayment(int paymentId, PaymentMethod paymentMethod, Principal principal) throws Exception {
        String username = principal.getName();
        User u = uServ.getUserByUsername(username); // Cần triển khai UserService
        String role = u.getRole().toString().toUpperCase();
        Payment payment = paymentRepository.getPaymentById(paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }

        if(!"PATIENT".equalsIgnoreCase(role)){
            try {
                throw new AccessDeniedException("Chỉ bệnh mới có quyền thực hiện thanh toán");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(!(u.getId() == payment.getAppointment().getPatient().getId()) ){
            try {
                throw new AccessDeniedException("Bạn không có quyền thực hiện thanh toán cho lịch hẹn này");
            } catch (AccessDeniedException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Cập nhật phương thức thanh toán
        payment.setPaymentMethod(paymentMethod);
        paymentRepository.updatePayment(payment);

        // Tạo yêu cầu thanh toán
        if (paymentMethod == PaymentMethod.Momo) {
            return createMoMoPayment(payment);
        } else if (paymentMethod == PaymentMethod.VNPay) {
            return createVNPAYPayment(payment);
        } else {
            throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ: " + paymentMethod);
        }
    }

    // Tạo yêu cầu thanh toán qua MoMo
    private String createMoMoPayment(Payment payment) throws Exception {
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = "ORDER_" + payment.getId() + "_" + System.currentTimeMillis();
        long amount = (long) (payment.getAmount() * 100); // MoMo yêu cầu đơn vị nhỏ nhất
        String extraData = "{}";
        String orderInfo = "ThanhToanLichHen" + payment.getAppointment().getId();
        String ipnUrl = PaymentConfig.NOTIFY_URL;
        String redirectUrl = PaymentConfig.RETURN_URL;

        // Tạo chữ ký
        String rawData = String.format("accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                PaymentConfig.MOMO_ACCESS_KEY, amount, extraData, ipnUrl, orderId, orderInfo,
                PaymentConfig.MOMO_PARTNER_CODE, redirectUrl, requestId, "captureWallet");
        
        System.out.println("rawData" + rawData);
        byte[] hmacSha256 = HmacUtils.hmacSha256(PaymentConfig.MOMO_SECRET_KEY, rawData);
        String signature = Hex.encodeHexString(hmacSha256);
        
        
        // Tạo body yêu cầu
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", PaymentConfig.MOMO_PARTNER_CODE);
        requestBody.put("accessKey", PaymentConfig.MOMO_ACCESS_KEY);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", String.valueOf(amount));
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", "ThanhToanLichHen" + payment.getAppointment().getId());
        requestBody.put("redirectUrl", PaymentConfig.RETURN_URL);
        requestBody.put("ipnUrl", PaymentConfig.NOTIFY_URL);
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", "captureWallet");
        requestBody.put("signature", signature);

        // Gửi yêu cầu đến MoMo
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(PaymentConfig.MOMO_ENDPOINT, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("payUrl")) {
            payment.setTransactionId(orderId);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.updatePayment(payment);
            return responseBody.get("payUrl").toString();
        } else {
            throw new RuntimeException("Không thể tạo yêu cầu thanh toán MoMo: " + responseBody);
        }
    }

    // Tạo yêu cầu thanh toán qua VNPAY
    private String createVNPAYPayment(Payment payment) throws Exception {
        String orderId = "ORDER_" + payment.getId() + "_" + System.currentTimeMillis();
        long amount = (long) (payment.getAmount() * 100); // VNPAY yêu cầu đơn vị nhỏ nhất

        // Tạo tham số yêu cầu
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", PaymentConfig.VNPAY_TMN_CODE);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Thanh toán lịch hẹn " + payment.getAppointment().getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", PaymentConfig.RETURN_URL);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        // Tạo chữ ký
        StringBuilder signData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signData.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        signData.deleteCharAt(signData.length() - 1);
        byte[] hmacSha256 = HmacUtils.hmacSha512(PaymentConfig.VNPAY_HASH_SECRET, signData.toString());
        String signature = Hex.encodeHexString(hmacSha256);

        vnpParams.put("vnp_SecureHash", signature);

        // Tạo URL thanh toán
        StringBuilder query = new StringBuilder(PaymentConfig.VNPAY_ENDPOINT).append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                 .append("=")
                 .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                 .append("&");
        }
        query.deleteCharAt(query.length() - 1);

        payment.setTransactionId(orderId);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.updatePayment(payment);

        return query.toString();
    }

    // Xử lý callback từ MoMo hoặc VNPAY
    @Override
    public void handlePaymentCallback(String paymentMethod, Map<String, String> params) {
        String transactionId = params.get("orderId");
        Payment payment = paymentRepository.getPaymentByTransactionId(transactionId);
        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }

        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            String resultCode = params.get("resultCode");
            if ("0".equals(resultCode)) { // Thanh toán thành công
                payment.setPaymentStatus(PaymentStatus.SUCCESSFUL);
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
            }
        } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) { // Thanh toán thành công
                payment.setPaymentStatus(PaymentStatus.SUCCESSFUL);
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
            }
        }

        paymentRepository.updatePayment(payment);
    }

}
