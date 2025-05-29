package com.can.controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Patient;
import com.can.pojo.User;
import com.can.services.StatisticService;
import com.can.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/statistics")
@CrossOrigin
public class ApiStatisticsController {
    @Autowired
    private StatisticService statisticService;

    @Autowired
    private UserService userService;

    // Thống kê số lượng bệnh nhân theo bác sĩ và khoảng thời gian
    @GetMapping("/doctor/patients-count")
    public ResponseEntity<?> countPatientsByDoctor(
            @RequestParam(name = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser == null) {
                return new ResponseEntity<>("Không tìm thấy thông tin người dùng", HttpStatus.UNAUTHORIZED);
            }
            
            // Kiểm tra người dùng hiện tại phải là bác sĩ
            if (!currentUser.getRole().name().equals("DOCTOR")) {
                return new ResponseEntity<>("Bạn không có quyền truy cập vào thống kê này", HttpStatus.FORBIDDEN);
            }
            
            int count = statisticService.countDistinctPatientsByDoctorAndDateRange(currentUser.getId(), fromDate, toDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Thống kê số lượng bệnh nhân theo bác sĩ và tháng
    @GetMapping("/doctor/patients-count-by-month")
    public ResponseEntity<?> countPatientsByDoctorAndMonth(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month) {
        try {
            // Xác thực người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser == null) {
                return new ResponseEntity<>("Không tìm thấy thông tin người dùng", HttpStatus.UNAUTHORIZED);
            }
            
            // Kiểm tra người dùng hiện tại phải là bác sĩ
            if (!currentUser.getRole().name().equals("DOCTOR")) {
                return new ResponseEntity<>("Bạn không có quyền truy cập vào thống kê này", HttpStatus.FORBIDDEN);
            }
            
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().body("Error: Month must be between 1 and 12.");
            }
            
            int count = statisticService.countDistinctPatientsByDoctorAndMonth(currentUser.getId(), year, month);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Thống kê số lượng bệnh nhân theo bác sĩ và quý
    @GetMapping("/doctor/patients-count-by-quarter")
    public ResponseEntity<?> countPatientsByDoctorAndQuarter(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "quarter") Integer quarter) {
        try {
            // Xác thực người dùng hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            
            if (currentUser == null) {
                return new ResponseEntity<>("Không tìm thấy thông tin người dùng", HttpStatus.UNAUTHORIZED);
            }
            
            // Kiểm tra người dùng hiện tại phải là bác sĩ
            if (!currentUser.getRole().name().equals("DOCTOR")) {
                return new ResponseEntity<>("Bạn không có quyền truy cập vào thống kê này", HttpStatus.FORBIDDEN);
            }
            
            if(quarter < 1 || quarter > 4) {
                return ResponseEntity.badRequest().body("Error: Quarter must be between 1 and 4.");
            }
            
            int count = statisticService.countDistinctPatientsByDoctorAndQuarter(currentUser.getId(), year, quarter);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}