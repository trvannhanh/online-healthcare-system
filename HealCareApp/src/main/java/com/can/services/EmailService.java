package com.can.services;

import com.can.pojo.Appointment;

/**
 *
 * @author DELL
 */
public interface EmailService {
    void sendEmail(String to, String subject, String content);
    //Gửi email xác nhận lịch hẹn khám bệnh cho bệnh nhân
    void sendAppointmentConfirmationEmail(Appointment appointment);
    //Gửi email thông báo lịch hẹn khám bệnh cho bác sĩ
    void sendAppointmentNotificationEmail(Appointment appointment);
    //Gửi email quảng cáo, ưu đãi đến bệnh nhân
    void sendPromotionalEmailToPatients(String promoContent);
}