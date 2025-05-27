package com.can.services.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.can.pojo.Appointment;
import com.can.pojo.Payment;
import com.can.services.StatisticService;
import com.can.repositories.StatisticRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author DELL
 */
@Service
public class StatisticServiceImpl implements StatisticService{

    @Autowired
    private StatisticRepository statisticRepo;

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
    public Integer countDistinctPatientsByDateRange(Date fromDateStr, Date toDateStr) throws ParseException {
        return this.statisticRepo.countDistinctPatientsByDateRange(fromDateStr, toDateStr);
    }

    @Override
    public Integer countDistinctPatientsByQuarter(int year, int quarter) throws ParseException {
        return this.statisticRepo.countDistinctPatientsByQuarter(year, quarter);
    }

    @Override
    public Integer countDistinctPatientsByMonth(int year, int month) throws ParseException {
        return this.statisticRepo.countDistinctPatientsByMonth(year, month);
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
    public List<Double> getMonthlyRevenue(int year) throws ParseException{
        return this.statisticRepo.getMonthlyRevenue(year);
    }

    @Override
    public List<Double> getQuarterlyRevenue(int year) throws ParseException{
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
}
