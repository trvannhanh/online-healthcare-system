package com.can.services.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.can.pojo.Appointment;
import com.can.pojo.Payment;
import com.can.pojo.User;
import com.can.services.DoctorService;
import com.can.services.StatisticService;
import com.can.services.UserService;
import com.can.repositories.StatisticRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    private StatisticRepository statisticRepo;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @Override
    public List<Appointment> getAppointmentsCompleteByDateRange(Date fromDateStr, Date toDateStr)
            throws ParseException {
        return this.statisticRepo.getAppointmentsCompleteByDateRange(fromDateStr, toDateStr);
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndDateRange(int doctorId, Date fromDateStr, Date toDateStr)
            throws ParseException {
        return this.statisticRepo.countDistinctPatientsByDoctorAndDateRange(doctorId, fromDateStr, toDateStr);
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndMonth(int doctorId, int year, int month) throws ParseException {
        return this.statisticRepo.countDistinctPatientsByDoctorAndMonth(doctorId, year, month);
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndQuarter(int doctorId, int year, int quarter) throws ParseException {

        return this.statisticRepo.countDistinctPatientsByDoctorAndQuarter(doctorId, year, quarter);
    }

    @Override
    public Integer countAppointmentsByDateRange(Date fromDateStr, Date toDateStr) throws ParseException {
        return this.statisticRepo.countAppointmentsByDateRange(fromDateStr, toDateStr);
    }

    @Override
    public Integer countAppointmentsByQuarter(int year, int quarter) throws ParseException {
        return this.statisticRepo.countAppointmentsByQuarter(year, quarter);
    }

    @Override
    public Integer countAppointmentsByMonth(int year, int month) throws ParseException {
        return this.statisticRepo.countAppointmentsByMonth(year, month);
    }

    @Override
    public List<Integer> getMonthlyStatistics(int year) throws ParseException {
        return this.statisticRepo.getMonthlyStatistics(year);
    }

    @Override
    public List<Integer> getQuarterlyStatistics(int year) throws ParseException {
        return this.statisticRepo.getQuarterlyStatistics(year);
    }

    @Override
    public List<Payment> getPaymentCompleteByDateRange(Date fromDateStr, Date toDateStr) throws ParseException {
        return this.statisticRepo.getPaymentCompleteByDateRange(fromDateStr, toDateStr);
    }

    @Override
    public Double getRevenueByDateRange(Date fromDate, Date toDate) throws ParseException {
        return this.statisticRepo.getRevenueByDateRange(fromDate, toDate);
    }

    @Override
    public Double getRevenueByQuarter(int year, int quarter) throws ParseException {
        return this.statisticRepo.getRevenueByQuarter(year, quarter);
    }

    @Override
    public Double getRevenueByMonth(int year, int month) throws ParseException {
        return this.statisticRepo.getRevenueByMonth(year, month);
    }

    @Override
    public List<Double> getMonthlyRevenue(int year) throws ParseException {
        return this.statisticRepo.getMonthlyRevenue(year);
    }

    @Override
    public List<Double> getQuarterlyRevenue(int year) throws ParseException {
        return this.statisticRepo.getQuarterlyRevenue(year);
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndDateRange(Date fromDate, Date toDate) throws ParseException {
        return this.statisticRepo.getRevenueByPaymentMethodAndDateRange(fromDate, toDate);
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndMonth(int year, int month) throws ParseException {
        return this.statisticRepo.getRevenueByPaymentMethodAndMonth(year, month);
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndQuarter(int year, int quarter) throws ParseException {
        return this.statisticRepo.getRevenueByPaymentMethodAndQuarter(year, quarter);
    }

    @Override
    public Map<String, Map<String, Object>> getPaymentMethodStats(Date fromDate, Date toDate) throws ParseException {
        return this.statisticRepo.getPaymentMethodStats(fromDate, toDate);
    }

    @Override
    public Map<Integer, Map<String, Double>> getMonthlyRevenueByPaymentMethod(int year) throws ParseException {
        return this.statisticRepo.getMonthlyRevenueByPaymentMethod(year);
    }

    @Override
    public Map<String, Long> getTopDiseaseTypesByDoctorSortedByMonth(String username, int year) throws ParseException {
        User currentUser = userService.getUserByUsername(username);

        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Chỉ bác sĩ mới có thể xem thống kê này");
        }

        if (!doctorService.isDoctorVerified(currentUser.getId())) {
            throw new RuntimeException("Bác sĩ chưa được xác minh không thể xem thống kê");
        }

        return this.statisticRepo.getTopDiseaseTypesByDoctorSortedByMonth(currentUser.getId(), year);
    }

    @Override
    public Map<String, Long> getTopDiseaseTypesByDoctorSortedByQuarter(String username, int year)
            throws ParseException {
        User currentUser = userService.getUserByUsername(username);

        if (!currentUser.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException("Chỉ bác sĩ mới có thể xem thống kê này");
        }

        if (!doctorService.isDoctorVerified(currentUser.getId())) {
            throw new RuntimeException("Bác sĩ chưa được xác minh không thể xem thống kê");
        }

        return this.statisticRepo.getTopDiseaseTypesByDoctorSortedByQuarter(currentUser.getId(), year);
    }
}
