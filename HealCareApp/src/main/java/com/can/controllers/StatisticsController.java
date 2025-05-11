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
import com.can.services.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.can.services.AppointmentService;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired
    private AppointmentService appointmentService;

    //Thống kê số lượng bệnh nhân theo quý
    @GetMapping("/patients-count-by-quarter")
    public ResponseEntity<?> countPatientsByQuarter(
            @RequestParam Integer year,
            @RequestParam Integer quarter) {
        try {
            int count = appointmentService.countDistinctPatientsByQuarter(year, quarter);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    //Thống kê số lượng bệnh nhân theo tháng
    @GetMapping("/patients-count-by-month")
    public ResponseEntity<?> countPatientsByMonth(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        try {
            int count = appointmentService.countDistinctPatientsByMonth(year, month);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
