package com.can.controllers;

import com.can.pojo.Response;
import com.can.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author DELL
 */
@RestController
@RequestMapping("/api/response")
public class ApiResponseController {
    
    @Autowired
    private ResponseService responseService;

    // Lấy phản hồi theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Response> getResponseById(@PathVariable Integer id) {
        Response response = responseService.getResponseById(id);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // Lấy tất cả phản hồi của bác sĩ theo ID
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Response>> getResponsesByDoctorId(@PathVariable Integer doctorId) {
        try {
            List<Response> responses = responseService.getResponsesByDoctorId(doctorId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Thêm một phản hồi mới
    @PostMapping
    public ResponseEntity<Response> addResponse(@RequestBody Response response) {
        try {
            Response newResponse = responseService.addResponse(response);
            return new ResponseEntity<>(newResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cập nhật phản hồi
    @PutMapping("/{id}")
    public ResponseEntity<Response> updateResponse(@PathVariable Integer id, @RequestBody Response response) {
        try {
            if (!responseService.isResponseExist(id)) {
                return ResponseEntity.notFound().build();
            }
            response.setId(id);
            Response updatedResponse = responseService.updateResponse(response);
            return ResponseEntity.ok(updatedResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa phản hồi
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable Integer id) {
        try {
            if (!responseService.isResponseExist(id)) {
                return ResponseEntity.notFound().build();
            }
            responseService.deleteResponse(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
