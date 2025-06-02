package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
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
    
    @Override
    public Rating addRating(Rating rating, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy user");
        }

        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ có bệnh nhân mới được đánh giá bác sĩ");
        }

        if (rating.getAppointment() == null) {
            throw new RuntimeException("Thông tin cuộc hẹn không tồn tại");
        }

        int appointmentId = rating.getAppointment().getId();
        if (!canPatientRateAppointment(appointmentId, currentUser.getId())) {
            throw new RuntimeException("Bạn không thể đánh giá bác sĩ này nếu lịch hẹn chưa hoàn tất.");
        }

        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new RuntimeException("Đánh giá phải trong khoảng từ 1 -> 5");
        }

        return this.ratingRepository.addRating(rating);
    }

    
    @Override
    public List<Rating> getAllRatings(Map<String, String> params) {
        return this.ratingRepository.getAllRatings(params);
    }

    @Override
    public Rating getRatingById(Integer id) {
        return this.ratingRepository.getRatingById(id);
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        return this.ratingRepository.getRatingsByDoctorId(doctorId);
    }

    

    @Override
    public Rating updateRating(Rating rating, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Người dùng không tìm thấy");
        }

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
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        if (!currentUser.getRole().name().equals("PATIENT")) {
            throw new RuntimeException("Chỉ bệnh nhân mới có thể xóa đánh giá về bác sĩ");
        }

        if (!isRatingExist(ratingId)) {
            throw new RuntimeException("Không tìm thấy đánh giá");
        }

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
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null) {
                return false;
            }

            if (appointment.getPatient().getId() != patientId) {
                return false;
            }

            if (!appointment.getStatus().equals(AppointmentStatus.COMPLETED)) {
                return false;
            }

            return !isAppointmentRated(appointmentId);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RatingResponse> getRatingResponsesByDoctorId(Integer doctorId) {
        List<Rating> ratings = this.ratingRepository.getRatingsByDoctorId(doctorId);

        List<RatingResponse> ratingResponses = new ArrayList<>();

        for (Rating rating : ratings) {
            RatingResponse ratingResponse = new RatingResponse();

            ratingResponse.setRating(rating);

            Response response = responseService.getResponsesByRating(rating.getId());

            ratingResponse.setResponse(response);

            ratingResponses.add(ratingResponse);
        }

        return ratingResponses;
    }

    @Override
    public Rating getRatingByAppointmentId(Integer appointmentId) {
       return this.ratingRepository.getRatingByAppointmentId(appointmentId);
    }
}
