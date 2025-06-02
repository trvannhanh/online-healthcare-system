package com.can.controllers;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.can.pojo.Payment;
import com.can.services.StatisticService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

@Controller
public class StatisticsController {
    @Autowired
    private StatisticService statisticService;

    @GetMapping("/statistics")
    public String showStatisticsPage() {
        return "statistics/statistics";
    }

    @GetMapping("statistics/patients-count-by-quarter")
    public String countPatientsByQuarter(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "quarter") Integer quarter,
            Model model) {
        try {
            int count = statisticService.countDistinctPatientsByQuarter(year, quarter);
            model.addAttribute("year", year);
            model.addAttribute("quarter", quarter);
            model.addAttribute("count", count);
            return "statistics/statistics"; 
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/patients-count-by-month")
    public String countPatientsByMonth(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month,
            Model model) {
        try {
            int count = statisticService.countDistinctPatientsByMonth(year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("count", count);

            return "statistics/statistics"; 
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage"; 
        }
    }

    @GetMapping("statistics/monthly-data-ajax")
    @ResponseBody
    public List<Integer> getMonthlyDataAjax(@RequestParam(name = "year") Integer year) {
        try {
            return statisticService.getMonthlyStatistics(year);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching monthly statistics for year: " + year);
            return List.of(); 
        }
    }

    @GetMapping("statistics/quarterly-data-ajax")
    @ResponseBody
    public List<Integer> getQuarterlyDataAjax(@RequestParam(name = "year") Integer year) {
        try {
            return statisticService.getQuarterlyStatistics(year);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching monthly statistics for year: " + year);
            return List.of(); 
        }
    }

    @GetMapping("statistics/revenue")
    public String showRevenueStatisticsPage(Model model) {
        return "statistics/revenue";
    }

    @GetMapping("statistics/revenue/payments")
    public String getPaymentsByDateRange(
            @RequestParam(name = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            Model model) {
        try {
            List<Payment> payments = statisticService.getPaymentCompleteByDateRange(fromDate, toDate);
            model.addAttribute("payments", payments);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            return "statistics/revenue";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/revenue/payments-date-range")
    public String getRevenueByDateRange(
            @RequestParam(name = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            Model model) {
        try {
            Double revenue = statisticService.getRevenueByDateRange(fromDate, toDate);
            model.addAttribute("revenue", revenue);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            return "statistics/revenue";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/revenue/payments-revenue-quarter")
    public String getRevenueByQuarter(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "quarter") Integer quarter,
            Model model) {
        try {
            Double revenue = statisticService.getRevenueByQuarter(year, quarter);
            model.addAttribute("year", year);
            model.addAttribute("quarter", quarter);
            model.addAttribute("revenue", revenue);
            return "statistics/revenue";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/revenue/payments-revenue-month")
    public String getRevenueByMonth(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month,
            Model model) {
        try {
            Double revenue = statisticService.getRevenueByMonth(year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("revenue", revenue);
            return "statistics/revenue";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/revenue/monthly-revenue-ajax")
    @ResponseBody
    public List<Double> getMonthlyRevenueAjax(@RequestParam(name = "year") Integer year) {
        try {
            return statisticService.getMonthlyRevenue(year);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); 
        }
    }

    @GetMapping("statistics/revenue/quarterly-revenue-ajax")
    @ResponseBody
    public List<Double> getQuarterlyRevenueAjax(@RequestParam(name = "year") Integer year) {
        try {
            return statisticService.getQuarterlyRevenue(year);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); 
        }
    }

    @GetMapping("statistics/revenue/payment-method/stats")
    public String getPaymentMethodStats(
            @RequestParam(name = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(name = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            Model model) {
        try {
            Map<String, Map<String, Object>> stats = statisticService.getPaymentMethodStats(fromDate, toDate);
            model.addAttribute("paymentMethodStats", stats);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            return "statistics/revenue";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/revenue/monthly-by-method")
    public String getMonthlyRevenueByPaymentMethod(
            @RequestParam(name = "year") Integer year,
            Model model) {
        try {
            Map<Integer, Map<String, Double>> monthlyData = statisticService.getMonthlyRevenueByPaymentMethod(year);
            model.addAttribute("monthlyRevenueByMethod", monthlyData);
            model.addAttribute("year", year);
            return "statistics/revenue";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "errorPage";
        }
    }

    @GetMapping("statistics/revenue/month-detail-revenue-ajax")
    @ResponseBody
    public Map<String, Object> getMonthDetailRevenueAjax(
            @RequestParam(name = "year") Integer year,
            @RequestParam(name = "month") Integer month) {
        try {
            Map<String, Double> revenueByMethod = statisticService.getRevenueByPaymentMethodAndMonth(year, month);

            Map<String, Object> result = new HashMap<>();
            result.put("labels", new ArrayList<>(revenueByMethod.keySet()));
            result.put("values", new ArrayList<>(revenueByMethod.values()));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("labels", List.of(), "values", List.of());
        }
    }
}
