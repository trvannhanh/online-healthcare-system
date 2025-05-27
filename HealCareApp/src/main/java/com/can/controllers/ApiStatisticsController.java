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
import com.can.services.StatisticService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/statistics")
public class ApiStatisticsController {
    @Autowired
    private StatisticService statisticService;

    // Thống kê số lượng bệnh nhân theo bác sĩ và khoảng thời gian
    @GetMapping("/doctor/patients-count")
    public ResponseEntity<?> countPatientsByDoctor(
            @RequestParam(name = "doctorId") Integer doctorId,
            @RequestParam(name = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {
        try {
            int count = statisticService.countDistinctPatientsByDoctorAndDateRange(doctorId, fromDate, toDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Thống kê số lượng bệnh nhân theo bác sĩ và tháng
    @GetMapping("/doctor/patients-count-by-month")
    public ResponseEntity<?> countPatientsByDoctorAndMonth(
            @RequestParam(name = "doctorId") Integer doctorId,
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().body("Error: Month must be between 1 and 12.");
            }
            int count = statisticService.countDistinctPatientsByDoctorAndMonth(doctorId, year, month);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Thống kê số lượng bệnh nhân theo bác sĩ và quý
    @GetMapping("/doctor/patients-count-by-quarter")
    public ResponseEntity<?> countPatientsByDoctorAndQuarter(
            @RequestParam(name = "doctorId") Integer doctorId,
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "quarter") Integer quarter) {
        try {
            if(quarter < 1 || quarter > 4) {
                return ResponseEntity.badRequest().body("Error: Quarter must be between 1 and 4.");
            }
            int count = statisticService.countDistinctPatientsByDoctorAndQuarter(doctorId, year, quarter);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
