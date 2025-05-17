package com.can.services.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.User;
import com.can.pojo.Patient;
import com.can.repositories.PatientRepository;
import com.can.services.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 *
 * @author DELL
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        // Gửi email
        javaMailSender.send(message);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Set true to indicate HTML content

            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle exception
        }
    }

    @Override
    public void sendAppointmentConfirmationEmail(Appointment appointment) {
        // Kiểm tra trạng thái lịch hẹn
        // Nếu không phải là lịch hẹn chưa xác nhận thì không gửi email
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return;
        }

        Patient patient = appointment.getPatient();
        User user = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        String doctorName = doctor.getUser().getFullName();

        String to = user.getEmail();
        String subject = "Xác nhận lịch hẹn khám bệnh";

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointment.getAppointmentDate());

        // Tạo link xác nhận lịch hẹn
        String confirmationLink = "http://localhost:3000/appointments/confirm/" + appointment.getId();

        // Tạo nội dung HTML với nút bấm
        String htmlContent = String.format(
                "<html>" +
                        "<head>" +
                        "  <style>" +
                        "    body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                        "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        "    .button { display: inline-block; background-color: #4CAF50; color: white; " +
                        "              padding: 12px 24px; text-decoration: none; border-radius: 4px; " +
                        "              font-weight: bold; margin-top: 20px; }" +
                        "    .button:hover { background-color: #45a049; }" +
                        "  </style>" +
                        "</head>" +
                        "<body>" +
                        "  <div class='container'>" +
                        "    <p>Xin chào %s,</p>" +
                        "    <p>Bạn đã đặt lịch hẹn với bác sĩ <strong>%s</strong> vào <strong>%s</strong>.</p>" +
                        "    <p>Vui lòng xác nhận lịch hẹn của bạn bằng cách nhấn vào nút bên dưới:</p>" +
                        "    <a href='%s' class='button'>Xác nhận lịch hẹn</a>" +
                        "    <p>Hoặc bạn có thể truy cập trực tiếp vào trang web của chúng tôi để xem chi tiết lịch hẹn:"
                        +
                        "      <a href='http://localhost:3000/appointments'>Xem lịch hẹn</a>" +
                        "    </p>" +
                        "    <p>Nếu bạn không thể giữ lịch hẹn, vui lòng liên hệ với chúng tôi để hủy hoặc đổi lịch.</p>"
                        +
                        "    <p>Trân trọng,<br>Đội ngũ Health Care</p>" +
                        "  </div>" +
                        "</body>" +
                        "</html>",
                user.getFullName(),
                doctorName,
                formattedDate,
                confirmationLink);

        // Gửi email HTML
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendAppointmentNotificationEmail(Appointment appointment) {
        // Kiểm tra trạng thái lịch hẹn
        // Nếu không phải là lịch hẹn đã xác nhận thì không gửi email
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return;
        }
        // Tính số ngày còn lại
        Date now = new Date();
        Date appointmentDate = appointment.getAppointmentDate();
        long diffInMillis = appointmentDate.getTime() - now.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        // Chỉ gửi email nếu cách lịch hẹn 1 đến 2 ngày
        if (diffInDays < 1 || diffInDays > 2) {
            return;
        }
        Doctor doctor = appointment.getDoctor();
        User user = doctor.getUser();
        String to = user.getEmail();
        String subject = "Thông báo lịch hẹn khám bệnh";

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointment.getAppointmentDate());

        String content = String.format(
                "Xin chào %s,\n\n" +
                        "Bạn có một lịch hẹn khám bệnh vào %s ngày %s.\n" +
                        "Vui lòng kiểm tra lại lịch trình của bạn.\n\n" +
                        "Trân trọng,\n",
                user.getFullName(),
                appointment.getPatient().getUser().getFullName(),
                formattedDate);
        // Gửi email
        sendEmail(to, subject, content);
    }

    // Gửi email khuyến mãi đến tất cả bệnh nhân
    @Override
    public void sendPromotionalEmailToPatients(String promoContent) {
        Map<String, String> params = new HashMap<>();
        List<Patient> patients = patientRepository.getPatients(params);
        for (Patient patient : patients) {
            // Lấy email của bệnh nhân
            User user = patient.getUser();
            String to = user.getEmail();
            String subject = "Ưu đãi đặc biệt từ phòng khám XYZ";

            // Cấu trúc nội dung email
            String content = String.format(
                    "Chào %s,\n\n" +
                            "Chúng tôi xin thông báo về ưu đãi đặc biệt mà bạn có thể tham gia ngay hôm nay!\n\n" +
                            "%s\n\n" +
                            "Trân trọng. \n",
                    user.getFullName(),
                    promoContent);

            // Gửi email
            sendEmail(to, subject, content);
        }
    }
}
