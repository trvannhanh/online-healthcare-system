package com.can.services.impl;

import java.util.List;
import java.util.Map;

import com.can.pojo.Doctor;
import com.can.services.ResponseService;
import com.can.pojo.Response;
import com.can.repositories.DoctorRepository;
import com.can.repositories.RatingRepository;
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
    private com.can.repositories.ResponseRepository responseRepo ;

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
    public Response addResponse(Response response) {
        // Implementation here
        return this.responseRepo.addResponse(response);
    }

    @Override
    public Response updateResponse(Response response) {
        // Implementation here
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
}
