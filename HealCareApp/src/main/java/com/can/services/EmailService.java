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
    void sendAppointmentNotificationEmail(Appointment appointment);
    void sendPromotionalEmailToPatients(String promoContent);
    void sendPaymentSuccessEmail(Payment payment);
}