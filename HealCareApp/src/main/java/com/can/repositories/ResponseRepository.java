package com.can.repositories;
import java.util.List;
import java.util.Map;
import com.can.pojo.Response;

/**
 *
 * @author DELL
 */
public interface ResponseRepository {
    List<Response> getAllResponses(Map<String, String> params);
    Response getResponseById(Integer id);
    List<Response> getResponsesByDoctorId(Integer doctorId);
    Response addResponse(Response response);
    Response updateResponse(Response response);
    void deleteResponse(Integer responseId);
    boolean isResponseExist(Integer responseId);
    Response getResponsesByRating(Integer ratingId);
} 
