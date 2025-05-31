package com.can.services;

import com.can.pojo.Response;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
public interface ResponseService {
    List<Response> getAllResponses(Map<String, String> params);
    Response getResponseById(Integer id);
    List<Response> getResponsesByDoctorId(Integer doctorId);
    Response addResponse(Response response, String username);
    Response updateResponse(Response response, String username);
    void deleteResponse(Integer responseId);
    boolean isResponseExist(int responseId);
    Response getResponsesByRating(Integer ratingId);  
    // Kiểm tra xem đánh giá đã được phản hồi chưa
    boolean isRatingResponsed(int ratingId);
    // Kiểm tra xem bác sĩ có thể phản hồi đánh giá không
    boolean canDoctorRespondToRating(int ratingId, int doctorId);

}
