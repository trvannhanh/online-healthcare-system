package com.can.controllers;

import java.util.Date;
import java.util.Map;
import com.can.pojo.User;
import com.can.services.StatisticService;
import com.can.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/secure")
@CrossOrigin
public class ApiStatisticsController {

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private UserService userService;

    @GetMapping("/statistics/doctor/patients-count")
    public ResponseEntity<?> countPatientsByDoctor(
            @RequestParam(name = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userService.getUserByUsername(username);

            int count = statisticService.countDistinctPatientsByDoctorAndDateRange(currentUser.getId(), fromDate, toDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/doctor/patients-count-by-month")
    public ResponseEntity<?> countPatientsByDoctorAndMonth(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month,
            Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userService.getUserByUsername(username);
            int count = statisticService.countDistinctPatientsByDoctorAndMonth(currentUser.getId(), year, month);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/doctor/patients-count-by-quarter")
    public ResponseEntity<?> countPatientsByDoctorAndQuarter(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "quarter") Integer quarter,
            Principal principal) {
        try {
            String username = principal.getName();
            User currentUser = userService.getUserByUsername(username);
            int count = statisticService.countDistinctPatientsByDoctorAndQuarter(currentUser.getId(), year, quarter);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/disease-type-by-month")
    public ResponseEntity<?> getTopDiseaseTypesByDoctorSortedByMonth(
            @RequestParam(name = "year", required = true) Integer year,
            Principal principal) {
        try {
            String username = principal.getName();
            Map<String, Long> diseaseStats = statisticService.getTopDiseaseTypesByDoctorSortedByMonth(username, year);
            return new ResponseEntity<>(diseaseStats, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/statistics/disease-type-by-quarter")
    public ResponseEntity<?> getTopDiseaseTypesByDoctorSortedByQuarter(
            @RequestParam(name = "year", required = true) Integer year,
            Principal principal) {
        try {
            String username = principal.getName();
            Map<String, Long> diseaseStats = statisticService.getTopDiseaseTypesByDoctorSortedByQuarter(username, year);
            return new ResponseEntity<>(diseaseStats, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
