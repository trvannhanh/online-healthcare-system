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
    Response addResponse(Response response);
    Response updateResponse(Response response);
    void deleteResponse(Integer responseId);
    boolean isResponseExist(int responseId);
}
