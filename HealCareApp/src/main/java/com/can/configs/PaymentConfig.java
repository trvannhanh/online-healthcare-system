/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Giidavibe
 */
@Configuration
public class PaymentConfig {
    // MoMo
    public static final String MOMO_PARTNER_CODE = "MOMO";
    public static final String MOMO_ACCESS_KEY = "F8BBA842ECF85";
    public static final String MOMO_SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    public static final String MOMO_ENDPOINT = "https://test-payment.momo.vn/v2/gateway/api/create"; // Sandbox URL

    // VNPAY
    public static final String VNPAY_TMN_CODE = "0C228W61";
    public static final String VNPAY_HASH_SECRET = "8LXZFHGSKTVNSCDL6D9LRYN661HFGM6K";
    public static final String VNPAY_ENDPOINT = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    public static final String RETURN_URL = "http://localhost:8080/HealCareApp/api/payment/return";
    public static final String NOTIFY_URL = "http://localhost:8080/HealCareApp/api/payment/notify";
}
