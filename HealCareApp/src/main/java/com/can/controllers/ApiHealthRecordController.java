//package com.can.controllers;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.can.pojo.HealthRecord;
//import com.can.services.HealthRecordService;
//import com.can.services.PatientService;
//import com.can.services.UserService;
//import com.can.pojo.Patient;
//import com.can.pojo.User;
//import com.can.repositories.PatientRepository;
//import com.can.services.PatientService;
//import com.can.services.UserService;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PatchMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.server.ResponseStatusException;
//
///**
// *
// * @author DELL
// */
//@RestController
//@RequestMapping("/api/secure/health-records")
//@CrossOrigin
//public class ApiHealthRecordController {
//
//    @Autowired
//    private HealthRecordService healthRecordService;
//
//    @Autowired
//    private PatientService patientService;
//
//    @Autowired
//    private UserService userService;
//
//    // Thêm mới một hồ sơ sức khỏe
//    @PostMapping("/add")
//    public ResponseEntity<?> createHealthRecord(@RequestBody HealthRecord healthRecord) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            User currentUser = userService.getUserByUsername(username);
//            // Nếu người dùng là bệnh nhân, tự động gán patient vào health record
//            if (currentUser.getRole().name().equals("PATIENT")) {
//                Patient patient = patientService.getPatientById(currentUser.getId());
//                healthRecord.setPatient(patient);
//            } else if (healthRecord.getPatient() == null) {
//                return new ResponseEntity<>("Thiếu thông tin bệnh nhân", HttpStatus.BAD_REQUEST);
//            }
//            HealthRecord newHealthRecord = healthRecordService.addHealthRecord(healthRecord);
//            return new ResponseEntity<>(newHealthRecord, HttpStatus.CREATED);
//        } catch (IllegalArgumentException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Dữ liệu không hợp lệ");
//            errorResponse.put("error", e.getMessage());
//            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//        } catch (RuntimeException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Không thể tạo hồ sơ sức khỏe");
//            errorResponse.put("error", e.getMessage());
//            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Lỗi hệ thống khi tạo hồ sơ sức khỏe");
//            errorResponse.put("error", e.getMessage());
//            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//            errorResponse.put("timestamp", LocalDateTime.now().toString());
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /// Cập nhật một hồ sơ sức khỏe theo id
//    @PutMapping("/{recordId}")
//    public ResponseEntity<?> updateHealthRecord(@PathVariable("recordId") Integer id,
//            @RequestBody HealthRecord healthRecord) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            healthRecord.setId(id.intValue());
//            HealthRecord existingRecord = healthRecordService.getHealthRecordById(id);
//            if (existingRecord == null) {
//                return new ResponseEntity<>("Không tìm thấy hồ sơ sức khỏe", HttpStatus.NOT_FOUND);
//            }
//            
//            // Kiểm tra quyền: chỉ bác sĩ hoặc chính bệnh nhân mới được cập nhật
//            User currentUser = userService.getUserByUsername(username);
//            String role = currentUser.getRole().name();
//            
//            if (!"DOCTOR".equals(role) && 
//                !(role.equals("PATIENT") && currentUser.getId() == existingRecord.getPatient().getId())) {
//                return new ResponseEntity<>("Bạn không có quyền cập nhật hồ sơ này", HttpStatus.FORBIDDEN);
//            }
//            
//            // Gán ID để cập nhật đúng record
//            healthRecord.setId(id);
//            
//            // Giữ nguyên thông tin bệnh nhân
//            if (healthRecord.getPatient() == null) {
//                healthRecord.setPatient(existingRecord.getPatient());
//            }
//            
//            // Giữ nguyên thông tin bác sĩ nếu không có cập nhật
//            if (healthRecord.getDoctor() == null) {
//                healthRecord.setDoctor(existingRecord.getDoctor());
//            }
//            
//            HealthRecord updatedHealthRecord = healthRecordService.updateHealthRecord(healthRecord);
//            return new ResponseEntity<>(updatedHealthRecord, HttpStatus.OK);
//        } catch (IllegalArgumentException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Dữ liệu không hợp lệ");
//            errorResponse.put("error", e.getMessage());
//            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//        } catch (RuntimeException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Không thể cập nhật hồ sơ sức khỏe");
//            errorResponse.put("error", e.getMessage());
//            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Lỗi hệ thống khi cập nhật hồ sơ sức khỏe");
//            errorResponse.put("error", e.getMessage());
//            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//            errorResponse.put("timestamp", LocalDateTime.now().toString());
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//}
