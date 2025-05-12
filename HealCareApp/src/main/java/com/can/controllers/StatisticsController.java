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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

import com.can.services.AppointmentService;

@Controller
public class StatisticsController {
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/statistics")
    public String showStatisticsPage() {
        return "statistics/statistics";
    }

    // Thống kê số lượng bệnh nhân theo quý
    @GetMapping("statistics/patients-count-by-quarter")
    public String countPatientsByQuarter(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "quarter") Integer quarter,
            org.springframework.ui.Model model) {
        try {
            int count = appointmentService.countDistinctPatientsByQuarter(year, quarter);
            model.addAttribute("year", year);
            model.addAttribute("quarter", quarter);
            model.addAttribute("count", count);
            return "statistics/statistics"; // Tên của file HTML hoặc JSP
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage"; // Tên của file HTML hoặc JSP cho trang lỗi
        }
    }

    // Thống kê số lượng bệnh nhân theo tháng
    @GetMapping("statistics/patients-count-by-month")
    public String countPatientsByMonth(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month,
            org.springframework.ui.Model model) {
        try {
            int count = appointmentService.countDistinctPatientsByMonth(year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("count", count);

            return "statistics/statistics"; // Tên của file HTML hoặc JSP
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage"; // Tên của file HTML hoặc JSP cho trang lỗi
        }
    }

    @GetMapping("statistics/monthly-data-ajax")
    @ResponseBody
    public List<Integer> getMonthlyDataAjax(@RequestParam(name = "year") Integer year) {
        try {
            // Lấy dữ liệu thống kê theo từng tháng
            return appointmentService.getMonthlyStatistics(year);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching monthly statistics for year: " + year);
            return List.of(); // Trả về danh sách rỗng nếu có lỗi
        }
    }

}
