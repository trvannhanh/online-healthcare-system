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
import com.can.pojo.Payment;
import com.can.pojo.PaymentStatus;
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
    public void sendAppointmentConfirmationEmail(Appointment appointment, User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("User or user email is missing");
        }

        // Get doctor's name
        Doctor doctor = appointment.getDoctor();
        String doctorName = doctor.getUser().getFullName();

        // Format date for email
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointment.getAppointmentDate());

        // Create confirmation link
        String confirmationLink = "http://localhost:3000/appointments/confirm/" + appointment.getId();

        // Create HTML content for email
        String htmlContent = createAppointmentConfirmationHtml(
                user.getFullName(),
                doctorName,
                formattedDate,
                confirmationLink);

        // Send HTML email
        sendHtmlEmail(user.getEmail(), "Xác nhận lịch hẹn khám bệnh", htmlContent);
    }

    /**
     * Creates HTML content for appointment confirmation email
     */
    private String createAppointmentConfirmationHtml(String userName, String doctorName,
            String formattedDate, String confirmationLink) {
        return "<html>" +
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
                "    <p>Xin chào " + userName + ",</p>" +
                "    <p>Bạn đã đặt lịch hẹn với bác sĩ <strong>" + doctorName + "</strong> vào <strong>" + formattedDate
                + "</strong>.</p>" +
                "    <p>Vui lòng xác nhận lịch hẹn của bạn bằng cách nhấn vào nút bên dưới:</p>" +
                "    <a href='" + confirmationLink + "' class='button'>Xác nhận lịch hẹn</a>" +
                "    <p>Hoặc bạn có thể truy cập trực tiếp vào trang web của chúng tôi để xem chi tiết lịch hẹn:" +
                "      <a href='http://localhost:3000/appointments'>Xem lịch hẹn</a>" +
                "    </p>" +
                "    <p>Nếu bạn không thể giữ lịch hẹn, vui lòng liên hệ với chúng tôi để hủy hoặc đổi lịch.</p>" +
                "    <p>Trân trọng,<br>Đội ngũ Health Care</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
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

    @Override
    public void sendInvoiceEmail(Payment payment) {
        // Kiểm tra trạng thái thanh toán
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESSFUL) {
            return; // Chỉ gửi khi thanh toán thành công
        }

        Patient patient = payment.getAppointment().getPatient();
        User user = patient.getUser();
        String to = user.getEmail();
        String subject = "Hóa đơn thanh toán - HealCare";

        // Tạo nội dung HTML với chi tiết hóa đơn
        String htmlContent = String.format(
                "<html>" +
                        "<head>" +
                        "  <style>" +
                        "    body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                        "    .invoice { max-width: 800px; margin: 0 auto; padding: 20px; border: 1px solid #eee; }" +
                        "    .header { text-align: center; margin-bottom: 20px; }" +
                        "    .details { margin-bottom: 20px; }" +
                        "    .details table { width: 100%; border-collapse: collapse; }" +
                        "    .details th, .details td { padding: 10px; text-align: left; border-bottom: 1px solid #eee; }"
                        +
                        "    .total { font-weight: bold; font-size: 18px; text-align: right; }" +
                        "  </style>" +
                        "</head>" +
                        "<body>" +
                        "  <div class='invoice'>" +
                        "    <div class='header'>" +
                        "      <h2>HÓA ĐƠN THANH TOÁN</h2>" +
                        "      <p>Mã giao dịch: %s</p>" +
                        "    </div>" +
                        "    <div class='details'>" +
                        "      <p><strong>Khách hàng:</strong> %s</p>" +
                        "      <p><strong>Bác sĩ:</strong> %s</p>" +
                        "      <p><strong>Ngày khám:</strong> %s</p>" +
                        "      <p><strong>Phương thức thanh toán:</strong> %s</p>" +
                        "      <table>" +
                        "        <tr><th>Dịch vụ</th><th>Số tiền</th></tr>" +
                        "        <tr><td>Phí khám bệnh</td><td>%s VND</td></tr>" +
                        "      </table>" +
                        "    </div>" +
                        "    <div class='total'>" +
                        "      <p>Tổng cộng: %s VND</p>" +
                        "    </div>" +
                        "    <div class='footer'>" +
                        "      <p>Cảm ơn bạn đã sử dụng dịch vụ của HealCare!</p>" +
                        "    </div>" +
                        "  </div>" +
                        "</body>" +
                        "</html>",
                payment.getTransactionId(),
                user.getFirstName() + " " + user.getLastName(),
                payment.getAppointment().getDoctor().getUser().getFirstName() + " " +
                        payment.getAppointment().getDoctor().getUser().getLastName(),
                new SimpleDateFormat("dd/MM/yyyy HH:mm").format(payment.getAppointment().getAppointmentDate()),
                payment.getPaymentMethod().toString(),
                String.format("%,.0f", payment.getAmount()),
                String.format("%,.0f", payment.getAmount()));

        // Gửi email HTML
        sendHtmlEmail(to, subject, htmlContent);
    }
}
