package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.Patient;
import com.can.pojo.Rating;
import com.can.pojo.RatingResponse;
import com.can.pojo.Response;
import com.can.pojo.User;

import com.can.repositories.RatingRepository;
import com.can.services.AppointmentService;
import com.can.services.RatingService;
import com.can.services.ResponseService;
import com.can.services.UserService;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ResponseService responseService;

    // Implement the methods defined in RatingService interface here
    @Override
    public List<Rating> getAllRatings(Map<String, String> params) {
        // Implementation code here
        return this.ratingRepository.getAllRatings(params);
    }

    @Override
    public Rating getRatingById(Integer id) {
        // Implementation code here
        return this.ratingRepository.getRatingById(id);
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        return this.ratingRepository.getRatingsByDoctorId(doctorId);
    }

    @Override
    public Rating addRating(Rating rating, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy user");
        }

        // Kiểm tra role - chỉ PATIENT mới được đánh giá
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ có bệnh nhân mới được đánh giá bác sĩ");
        }

        // 3. Kiểm tra cuộc hẹn có tồn tại không
        if (rating.getAppointment() == null) {
            throw new RuntimeException("Thông tin cuộc hẹn không tồn tại");
        }

        int appointmentId = rating.getAppointment().getId();
        if (!canPatientRateAppointment(appointmentId, currentUser.getId())) {
            throw new RuntimeException(
                    "Bạn không thể đánh giá bác sĩ này nếu lịch hẹn chưa hoàn tất.");
        }

        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new RuntimeException("Đánh giá phải trong khoảng từ 1 -> 5");
        }

        return this.ratingRepository.addRating(rating);
    }

    @Override
    public Rating updateRating(Rating rating, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Kiểm tra role - chỉ PATIENT mới được đánh giá
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ bệnh nhân mới có thể cập nhật lại đánh giá về bác sĩ");
        }

        if (!isRatingExist(rating.getId())) {
            throw new RuntimeException("Không tìm thấy đánh giá");
        }

        return this.ratingRepository.updateRating(rating);
    }

    @Override
    public void deleteRating(Integer ratingId, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Kiểm tra role - chỉ PATIENT mới được đánh giá
        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ bệnh nhân mới có thể xóa đánh giá về bác sĩ");
        }

        // Kiểm tra xem đánh giá có tồn tại không
        if (!isRatingExist(ratingId)) {
            throw new RuntimeException("Không tìm thấy đánh giá");
        }

        // Lấy và xóa phản hồi liên quan đến đánh giá
        Response response = responseService.getResponsesByRating(ratingId);
        if (response != null) {
            responseService.deleteResponse(response.getId());
        }

        this.ratingRepository.deleteRating(ratingId);
    }

    @Override
    public boolean isRatingExist(int ratingId) {
        return this.ratingRepository.isRatingExist(ratingId);
    }

    @Override
    public Double getAverageRatingForDoctor(Integer doctorId) {
        return this.ratingRepository.getAverageRatingForDoctor(doctorId);
    }

    @Override
    public boolean isAppointmentRated(int appointmentId) {
        return ratingRepository.isAppointmentRated(appointmentId);
    }

    @Override
    public boolean canPatientRateAppointment(int appointmentId, int patientId) {
        try {
            // 1. Kiểm tra xem cuộc hẹn có tồn tại không
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null) {
                return false;
            }

            // 2. Kiểm tra xem người đánh giá có phải là bệnh nhân trong cuộc hẹn không
            if (appointment.getPatient().getId() != patientId) {
                return false;
            }

            // 3. Kiểm tra xem cuộc hẹn đã hoàn thành chưa
            if (!appointment.getStatus().equals(AppointmentStatus.COMPLETED)) {
                return false;
            }

            // 4. Kiểm tra xem cuộc hẹn đã được đánh giá chưa
            return !isAppointmentRated(appointmentId);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RatingResponse> getRatingResponsesByDoctorId(Integer doctorId) {
        // 1. Lấy danh sách đánh giá cho bác sĩ
        List<Rating> ratings = this.ratingRepository.getRatingsByDoctorId(doctorId);

        // 2. Tạo danh sách kết quả
        List<RatingResponse> ratingResponses = new ArrayList<>();

        // 3. Duyệt qua từng đánh giá và tìm response tương ứng (nếu có)
        for (Rating rating : ratings) {
            // Tạo đối tượng RatingResponse mới
            RatingResponse ratingResponse = new RatingResponse();

            // Thiết lập thông tin từ Rating
            ratingResponse.setRating(rating);

            // Tìm response tương ứng với rating này
            Response response = responseService.getResponsesByRating(rating.getId());

            // Thiết lập response vào RatingResponse (có thể null)
            ratingResponse.setResponse(response);

            // Thêm vào danh sách kết quả
            ratingResponses.add(ratingResponse);
        }

        return ratingResponses;
    }

    @Override
    public Rating getRatingByAppointmentId(Integer appointmentId) {
       return this.ratingRepository.getRatingByAppointmentId(appointmentId);
    }
}
