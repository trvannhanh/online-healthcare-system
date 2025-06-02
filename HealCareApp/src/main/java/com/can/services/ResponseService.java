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
    boolean isRatingResponsed(int ratingId);
    boolean canDoctorRespondToRating(int ratingId, int doctorId);

}
