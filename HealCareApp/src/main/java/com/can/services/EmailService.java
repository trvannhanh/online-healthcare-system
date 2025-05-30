package com.can.services;

import com.can.pojo.Appointment;
import com.can.pojo.Payment;
import com.can.pojo.User;

/**
 *
 * @author DELL
 */
public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception;
    void sendAppointmentConfirmationEmail(Appointment appointment, User user) throws Exception;
    //Gửi email thông báo lịch hẹn khám bệnh cho bác sĩ
    void sendAppointmentNotificationEmail(Appointment appointment);
    //Gửi email quảng cáo, ưu đãi đến bệnh nhân
    void sendPromotionalEmailToPatients(String promoContent);
    //Gửi email hóa đơn thanh toán
//    void sendInvoiceEmail(Payment payment);
}