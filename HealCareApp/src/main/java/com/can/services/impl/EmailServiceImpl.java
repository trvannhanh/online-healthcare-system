package com.can.services.impl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.User;
import com.can.pojo.Patient;
import com.can.pojo.Payment;
import com.can.repositories.PatientRepository;
import com.can.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

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
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            javaMailSender.send(message);
            System.out.println("Email sent successfully to: " + to + " with subject: " + subject);
        } catch (MessagingException e) {
            System.out.println("Failed to send email to: " + to + ". Error: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws Exception {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (MessagingException e) {
            System.out.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new Exception("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendAppointmentConfirmationEmail(Appointment appointment, User user) throws Exception {
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            System.out.println("Invalid user or email for appointment ID " + appointment.getId());
            throw new IllegalArgumentException("Thông tin người dùng hoặc email không hợp lệ");
        }

        Doctor doctor = appointment.getDoctor();
        if (doctor == null || doctor.getUser() == null) {
            System.out.println("Invalid doctor for appointment ID " + appointment.getId());
            throw new IllegalArgumentException("Thông tin bác sĩ không hợp lệ");
        }

        String doctorName = doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName();
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointment.getAppointmentDate());
        String confirmationLink = frontendUrl + "/appointment";

        String htmlContent = createAppointmentConfirmationHtml(
                user.getFirstName() + " " + user.getLastName(),
                doctorName,
                formattedDate,
                confirmationLink,
                doctor.getSpecialization().getName(),
                doctor.getHospital().getName()
        );

        try {
            sendHtmlEmail(user.getEmail(), "Xác Nhận Lịch Hẹn Khám Bệnh", htmlContent);
        } catch (Exception e) {
            System.out.println("Lỗi gửi email xác nhận cho lịch hẹn " + appointment.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    private String createAppointmentConfirmationHtml(String patientName, String doctorName, String appointmentTime,
            String confirmationLink, String specialization, String hospital) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Xác Nhận Lịch Hẹn</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                        .header { text-align: center; padding: 10px 0; background: #007bff; color: #fff; border-radius: 10px 10px 0 0; }
                        .header h1 { margin: 0; font-size: 24px; }
                        .content { padding: 20px; }
                        .content p { margin: 10px 0; }
                        .button { display: inline-block; padding: 10px 20px; background: #28a745; color: #fff; text-decoration: none; border-radius: 5px; }
                        .button:hover { background: #218838; }
                        .footer { text-align: center; padding: 10px; font-size: 12px; color: #777; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Xác Nhận Lịch Hẹn</h1>
                        </div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Bạn đã đặt lịch hẹn thành công với:</p>
                            <p><strong>Bác sĩ:</strong> %s</p>
                            <p><strong>Chuyên khoa:</strong> %s</p>
                            <p><strong>Bệnh viện:</strong> %s</p>
                            <p><strong>Thời gian:</strong> %s</p>
                            <p>Theo dõi lịch hẹn bằng cách nhấn vào nút dưới đây:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="button">Xem Lịch Hẹn</a>
                            </p>
                            <p>Nếu bạn không thể nhấn nút, hãy sao chép và dán liên kết sau vào trình duyệt:</p>
                            <p>%s</p>
                        </div>
                        <div class="footer">
                            <p>Đây là email tự động, vui lòng không trả lời. Nếu cần hỗ trợ, liên hệ qua email support@hospital.com.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(patientName, doctorName, specialization, hospital, appointmentTime, confirmationLink, confirmationLink);
    }

    @Override
    public void sendAppointmentNotificationEmail(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return;
        }
        Date now = new Date();
        Date appointmentDate = appointment.getAppointmentDate();
        long diffInMillis = appointmentDate.getTime() - now.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        if (diffInDays < 1 || diffInDays > 2) {
            return;
        }
        Doctor doctor = appointment.getDoctor();
        User user = doctor.getUser();
        String to = user.getEmail();
        String subject = "Thông báo lịch hẹn khám bệnh";

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointment.getAppointmentDate());

        String content = String.format(
                "Xin chào %s,\n\n"
                + "Bạn có một lịch hẹn khám bệnh vào %s ngày %s.\n"
                + "Vui lòng kiểm tra lại lịch trình của bạn.\n\n"
                + "Trân trọng,\n",
                user.getFullName(),
                appointment.getPatient().getUser().getFullName(),
                formattedDate);
        sendEmail(to, subject, content);
    }
    
    
    @Override
    public void sendPaymentSuccessEmail(Payment payment) {
        try {
            String patientEmail = payment.getAppointment().getPatient().getUser().getEmail();
            String subject = "Xác nhận thanh toán thành công - HealCareApp";
            String body = "<h2>Thanh toán thành công</h2>"
                    + "<p>Kính gửi Quý khách,</p>"
                    + "<p>Chúng tôi xin xác nhận thanh toán của bạn đã hoàn tất. Dưới đây là chi tiết:</p>"
                    + "<ul>"
                    + "<li><b>Mã hóa đơn:</b> " + payment.getId() + "</li>"
                    + "<li><b>Mã giao dịch:</b> " + payment.getTransactionId() + "</li>"
                    + "<li><b>Số tiền:</b> " + String.format("%,.0f VNĐ", payment.getAmount()) + "</li>"
                    + "<li><b>Phương thức:</b> " + payment.getPaymentMethod() + "</li>"
                    + "<li><b>Ngày thanh toán:</b> " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(payment.getCreateAt()) + "</li>"
                    + "<li><b>Lịch hẹn:</b> " + payment.getAppointment().getId() + "</li>"
                    + "</ul>"
                    + "<p>Cảm ơn bạn đã sử dụng dịch vụ của HealCareApp!</p>"
                    + "<p>Trân trọng,<br>Đội ngũ HealCareApp</p>";

            this.sendEmail(patientEmail, subject, body);
        } catch (Exception e) {
            System.out.println("Failed to send payment success email for payment ID: " + payment.getId() + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void sendPromotionalEmailToPatients(String promoContent) {
        Map<String, String> params = new HashMap<>();
        List<Patient> patients = patientRepository.getPatients(params);
        for (Patient patient : patients) {
            User user = patient.getUser();
            String to = user.getEmail();
            String subject = "Ưu đãi đặc biệt từ phòng khám XYZ";

            String content = String.format(
                    "Chào %s,\n\n"
                    + "Chúng tôi xin thông báo về ưu đãi đặc biệt mà bạn có thể tham gia ngay hôm nay!\n\n"
                    + "%s\n\n"
                    + "Trân trọng. \n",
                    user.getFullName(),
                    promoContent);

            sendEmail(to, subject, content);
        }
    }

}
