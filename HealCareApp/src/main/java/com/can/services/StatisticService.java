package com.can.services;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.can.pojo.Appointment;
import com.can.pojo.Payment;

/**
 *
 * @author DELL
 */
public interface StatisticService {
    List<Appointment> getAppointmentsCompleteByDateRange(Date fromDateStr, Date toDateStr) throws ParseException;

    Integer countAppointmentsByDateRange(Date fromDateStr, Date toDateStr) throws ParseException;

    Integer countAppointmentsByQuarter(int year, int quarter) throws ParseException;

    Integer countAppointmentsByMonth(int year, int month) throws ParseException;

    Integer countDistinctPatientsByDoctorAndDateRange(int doctorId, Date fromDateStr, Date toDateStr)
            throws ParseException;

    Integer countDistinctPatientsByDoctorAndMonth(int doctorId, int year, int month) throws ParseException;

    Integer countDistinctPatientsByDoctorAndQuarter(int doctorId, int year, int quarter) throws ParseException;

    List<Integer> getMonthlyStatistics(int year) throws ParseException;

    List<Integer> getQuarterlyStatistics(int year) throws ParseException;

    List<Payment> getPaymentCompleteByDateRange(Date fromDateStr, Date toDateStr) throws ParseException;

    Double getRevenueByDateRange(Date fromDate, Date toDate) throws ParseException;

    Double getRevenueByQuarter(int year, int quarter) throws ParseException;

    Double getRevenueByMonth(int year, int month) throws ParseException;

    List<Double> getMonthlyRevenue(int year) throws ParseException;

    List<Double> getQuarterlyRevenue(int year) throws ParseException;

    Map<String, Double> getRevenueByPaymentMethodAndDateRange(Date fromDate, Date toDate) throws ParseException;

    Map<String, Double> getRevenueByPaymentMethodAndMonth(int year, int month) throws ParseException;

    Map<String, Double> getRevenueByPaymentMethodAndQuarter(int year, int quarter) throws ParseException;

    Map<String, Map<String, Object>> getPaymentMethodStats(Date fromDate, Date toDate) throws ParseException;

    Map<Integer, Map<String, Double>> getMonthlyRevenueByPaymentMethod(int year) throws ParseException;

    //Thống kê loại bệnh phổ biến nhất trong một khoảng thời gian
    // Map<String, Long> getTopDiseaseTypes(Date fromDate, Date toDate, int topN) throws ParseException;
    // Map<Integer, Map<String, Long>> getMostCommonDiseaseByMonth(int year) throws ParseException;
    // Map<Integer, Map<String, Long>> getMostCommonDiseaseByQuarter(int year) throws ParseException;
    Map<String, Long> getTopDiseaseTypesByDoctorSortedByMonth(String username, int year) throws ParseException;
    Map<String, Long> getTopDiseaseTypesByDoctorSortedByQuarter(String username, int year) throws ParseException;

}
