package com.can.services.impl;

import java.util.List;
import java.util.Map;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.Rating;
import com.can.pojo.User;
import com.can.services.DoctorService;
import com.can.services.RatingService;
import com.can.services.ResponseService;
import com.can.services.UserService;
import com.can.pojo.Response;
import com.can.repositories.ResponseRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class ResponseServiceImpl implements ResponseService {

    @Autowired
    private ResponseRepository responseRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private RatingService ratingService;

    @Override
    public List<Response> getAllResponses(Map<String, String> params) {
        // Implementation here
        return this.responseRepo.getAllResponses(params);
    }

    @Override
    public Response getResponseById(Integer id) {
        // Implementation here
        return this.responseRepo.getResponseById(id);
    }

    @Override
    public List<Response> getResponsesByDoctorId(Integer doctorId) {
        return this.responseRepo.getResponsesByDoctorId(doctorId);
    }

    @Override
    public Response addResponse(Response response, String username) {
        // Kiểm tra user có tồn tại không
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }
        // Kiểm tra role - chỉ DOCTOR mới được phản hồi đánh giá
        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Chỉ bác sĩ mới được phép phản hồi đánh giá");
        }

        // Kiểm tra rating có tồn tại không
        if (response.getRating() == null ) {
            throw new RuntimeException("Thông tin đánh giá không hợp lệ");
        }

        // Sử dụng phương thức canDoctorRespondToRating để kiểm tra
        boolean canRespond = canDoctorRespondToRating(response.getRating().getId(), currentUser.getId());
        if (!canRespond) {
            throw new RuntimeException("Bác sĩ không có quyền phản hồi đánh giá này hoặc đánh giá đã có phản hồi");
        }
        
        response.setResponseDate(new Date());

        return this.responseRepo.addResponse(response);
    }

    @Override
    public Response updateResponse(Response response, String username) {
        // Kiểm tra user có tồn tại không
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        // Kiểm tra role - chỉ DOCTOR mới được phản hồi đánh giá
        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Chỉ bác sĩ mới được phép cập nhật phản hồi");
        }

        // Lấy phản hồi hiện tại
        Response existingResponse = responseRepo.getResponseById(response.getId());
        if (existingResponse == null) {
            throw new RuntimeException("Không tìm thấy phản hồi cần cập nhật");
        }

        // Kiểm tra bác sĩ chỉ cập nhật phản hồi của mình
        Integer doctorIdInResponse = existingResponse.getRating().getAppointment().getDoctor().getId();
        if (!doctorIdInResponse.equals(currentUser.getId())) {
            throw new RuntimeException("Bác sĩ chỉ được cập nhật phản hồi của chính mình");
        }

        if (!isResponseExist(response.getId())) {
            throw new RuntimeException("Không tìm thấy phản hồi");
        }

        return this.responseRepo.updateResponse(response);
    }

    @Override
    public void deleteResponse(Integer id) {
        // Implementation here
        this.responseRepo.deleteResponse(id);
    }

    @Override
    public boolean isResponseExist(int id) {
        // Implementation here
        return this.responseRepo.isResponseExist(id);
    }

    @Override
    public Response getResponsesByRating(Integer ratingId) {
        // Implementation here
        return this.responseRepo.getResponsesByRating(ratingId);
    }

    @Override
    public boolean isRatingResponsed(int ratingId) {
        return responseRepo.isRatingResponsed(ratingId);
    }

    @Override
    public boolean canDoctorRespondToRating(int ratingId, int doctorId) {
        try {
            // 1. Kiểm tra xem đánh giá có tồn tại không
            Rating rating = ratingService.getRatingById(ratingId);
            if (rating == null) {
                return false;
            }

            // 2. Kiểm tra xem bác sĩ được đánh giá có phải là bác sĩ đang đăng nhập không
            if (rating.getAppointment().getDoctor().getId() != doctorId) {
                return false;
            }

            // 3. Kiểm tra xem bác sĩ đã được xác minh chưa
            Doctor doctor = doctorService.getDoctorById(doctorId);
            if (doctor == null || !doctor.isIsVerified()) {
                return false;
            }

            // 4. Kiểm tra xem đánh giá này đã có phản hồi chưa
            return !isRatingResponsed(ratingId);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
