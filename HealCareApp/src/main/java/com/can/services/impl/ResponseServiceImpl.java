package com.can.services.impl;

import java.util.List;
import java.util.Map;
import com.can.pojo.Doctor;
import com.can.pojo.Rating;
import com.can.pojo.User;
import com.can.services.DoctorService;
import com.can.services.RatingService;
import com.can.services.ResponseService;
import com.can.services.UserService;
import com.can.pojo.Response;
import com.can.repositories.ResponseRepository;
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
    public Response addResponse(Response response, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }
        
        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Chỉ bác sĩ mới được phép phản hồi đánh giá");
        }

        if (response.getRating() == null ) {
            throw new RuntimeException("Thông tin đánh giá không hợp lệ");
        }

        boolean canRespond = canDoctorRespondToRating(response.getRating().getId(), currentUser.getId());
        if (!canRespond) {
            throw new RuntimeException("Bác sĩ không có quyền phản hồi đánh giá này hoặc đánh giá đã có phản hồi");
        }
        

        return this.responseRepo.addResponse(response);
    }
    
    @Override
    public boolean canDoctorRespondToRating(int ratingId, int doctorId) {
        try {
            Rating rating = ratingService.getRatingById(ratingId);
            if (rating == null) {
                return false;
            }

            if (rating.getAppointment().getDoctor().getId() != doctorId) {
                return false;
            }

            Doctor doctor = doctorService.getDoctorById(doctorId);
            if (doctor == null || !doctor.isIsVerified()) {
                return false;
            }

            return !isRatingResponsed(ratingId);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Response> getAllResponses(Map<String, String> params) {
        return this.responseRepo.getAllResponses(params);
    }

    @Override
    public Response getResponseById(Integer id) {
        return this.responseRepo.getResponseById(id);
    }

    @Override
    public List<Response> getResponsesByDoctorId(Integer doctorId) {
        return this.responseRepo.getResponsesByDoctorId(doctorId);
    }

    @Override
    public Response updateResponse(Response response, String username) {
        User currentUser = userService.getUserByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("User not found");
        }

        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Chỉ bác sĩ mới được phép cập nhật phản hồi");
        }

        Response existingResponse = responseRepo.getResponseById(response.getId());
        if (existingResponse == null) {
            throw new RuntimeException("Không tìm thấy phản hồi cần cập nhật");
        }

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
        this.responseRepo.deleteResponse(id);
    }

    @Override
    public boolean isResponseExist(int id) {
        return this.responseRepo.isResponseExist(id);
    }

    @Override
    public Response getResponsesByRating(Integer ratingId) {
        return this.responseRepo.getResponsesByRating(ratingId);
    }

    @Override
    public boolean isRatingResponsed(int ratingId) {
        return responseRepo.isRatingResponsed(ratingId);
    }

    
}
