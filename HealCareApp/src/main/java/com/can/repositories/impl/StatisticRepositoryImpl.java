package com.can.repositories.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.HealthRecord;
import com.can.pojo.Patient;
import com.can.pojo.Payment;
import com.can.pojo.PaymentMethod;
import com.can.repositories.StatisticRepository;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.can.repositories.ResponseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class StatisticRepositoryImpl implements StatisticRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Appointment> getAppointmentsCompleteByDateRange(Date fromDateStr, Date toDateStr)
            throws ParseException {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Appointment> q = b.createQuery(Appointment.class);
        Root<Appointment> root = q.from(Appointment.class);
        root.fetch("doctor").fetch("user");
        root.fetch("patient").fetch("user");

        q.where(
                b.equal(root.get("status"), AppointmentStatus.COMPLETED),
                b.between(root.get("appointmentDate"), fromDateStr, toDateStr));
        q.orderBy(b.asc(root.get("appointmentDate")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndDateRange(int doctorId, Date fromDateStr, Date toDateStr)
            throws ParseException {
        List<Appointment> appointments = getAppointmentsCompleteByDateRange(fromDateStr, toDateStr);

        return (int) appointments.stream()
                .filter(a -> a.getDoctor().getId() == doctorId)
                .map(a -> a.getPatient().getId())
                .distinct()
                .count();
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndMonth(int doctorId, int year, int month) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();
        return countDistinctPatientsByDoctorAndDateRange(doctorId, fromDate, toDate);
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndQuarter(int doctorId, int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1; 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2); 
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return countDistinctPatientsByDoctorAndDateRange(doctorId, fromDate, toDate);
    }

    @Override
    public Integer countAppointmentsByDateRange(Date fromDateStr, Date toDateStr) throws ParseException {
        List<Appointment> appointments = this.getAppointmentsCompleteByDateRange(fromDateStr, toDateStr);

        return (int) appointments.size();
    }

    @Override
    public Integer countAppointmentsByQuarter(int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1; 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2); 
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return countAppointmentsByDateRange(fromDate, toDate);
    }

    @Override
    public Integer countAppointmentsByMonth(int year, int month) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return countAppointmentsByDateRange(fromDate, toDate);
    }

    @Override
    public List<Integer> getMonthlyStatistics(int year) throws ParseException {
        try {
            List<Integer> monthlyData = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                monthlyData.add(countAppointmentsByMonth(year, i));
            }
            return monthlyData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy dữ liệu thống kê");
        }
    }

    @Override
    public List<Integer> getQuarterlyStatistics(int year) throws ParseException {
        try {
            List<Integer> quarterlyData = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                quarterlyData.add(countAppointmentsByQuarter(year, i));
            }
            return quarterlyData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy dữ liệu thống kê");
        }
    }

    @Override
    public List<Payment> getPaymentCompleteByDateRange(Date fromDateStr, Date toDateStr)
            throws ParseException {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Payment> q = b.createQuery(Payment.class);
        Root<Payment> root = q.from(Payment.class);

        q.where(
                b.equal(root.get("paymentStatus"), "SUCCESSFUL"),
                b.between(root.get("createdAt"), fromDateStr, toDateStr));
        q.orderBy(b.asc(root.get("createdAt")));

        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public Double getRevenueByDateRange(Date fromDate, Date toDate) throws ParseException {
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        Double totalRevenue = payments.stream()
                .mapToDouble(payment -> payment.getAmount())
                .sum();

        return totalRevenue;
    }

    @Override
    public Double getRevenueByQuarter(int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1; 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2); 
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        Double totalRevenue = payments.stream()
                .mapToDouble(payment -> payment.getAmount())
                .sum();

        return totalRevenue;
    }

    @Override
    public Double getRevenueByMonth(int year, int month) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return getRevenueByDateRange(fromDate, toDate);
    }

    @Override
    public List<Double> getMonthlyRevenue(int year) throws ParseException {
        try {
            List<Double> monthlyData = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                monthlyData.add(getRevenueByMonth(year, i));
            }
            return monthlyData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy dữ liệu thống kê");
        }
    }

    @Override
    public List<Double> getQuarterlyRevenue(int year) throws ParseException {
        try {
            List<Double> quarterlyData = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                quarterlyData.add(getRevenueByQuarter(year, i));
            }
            return quarterlyData;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy dữ liệu thống kê");
        }
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndDateRange(Date fromDate, Date toDate) throws ParseException {
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        Map<String, Double> result = new HashMap<>();

        for (PaymentMethod method : PaymentMethod.values()) {
            String methodName = method.toString();

            Double total = payments.stream()
                    .filter(p -> p.getPaymentMethod() == method)
                    .mapToDouble(Payment::getAmount)
                    .sum();

            result.put(methodName, total);
        }

        return result;
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndMonth(int year, int month) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return getRevenueByPaymentMethodAndDateRange(fromDate, toDate);
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndQuarter(int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return getRevenueByPaymentMethodAndDateRange(fromDate, toDate);
    }

    @Override
    public Map<String, Map<String, Object>> getPaymentMethodStats(Date fromDate, Date toDate) throws ParseException {
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (PaymentMethod method : PaymentMethod.values()) {
            String methodName = method.toString();

            List<Payment> methodPayments = payments.stream()
                    .filter(p -> p.getPaymentMethod() == method)
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("count", methodPayments.size()); 

            if (methodPayments.isEmpty()) {
                stats.put("total", 0.0);
                stats.put("average", 0.0);
                stats.put("min", 0.0);
                stats.put("max", 0.0);
            } else {
                DoubleSummaryStatistics summaryStatistics = methodPayments.stream()
                        .mapToDouble(Payment::getAmount)
                        .summaryStatistics();

                stats.put("total", summaryStatistics.getSum()); 
                stats.put("average", summaryStatistics.getAverage()); 
                stats.put("min", summaryStatistics.getMin()); 
                stats.put("max", summaryStatistics.getMax()); 
            }

            result.put(methodName, stats);
        }

        return result;
    }

    @Override
    public Map<Integer, Map<String, Double>> getMonthlyRevenueByPaymentMethod(int year) throws ParseException {
        Map<Integer, Map<String, Double>> result = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            Map<String, Double> monthRevenue = getRevenueByPaymentMethodAndMonth(year, month);
            result.put(month, monthRevenue);
        }

        return result;
    }


    @Override
    public Map<String, Long> getTopDiseaseTypesByDoctorSortedByMonth(int doctorId, int year)
            throws ParseException {
        Session s = this.factory.getObject().getCurrentSession();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-01-01");
        Date toDate = sdf.parse((year + 1) + "-01-01");

        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("appointment");
        q.distinct(true);

        q.where(
                b.and(
                        b.equal(root.get("appointment").get("doctor").get("id"), doctorId),
                        b.between(root.get("createdDate"), fromDate, toDate),
                        b.isNotNull(root.get("diseaseType")),
                        b.notEqual(root.get("diseaseType"), "")));

        List<HealthRecord> records = s.createQuery(q).getResultList();

        Map<String, Long> diseaseCount = records.stream()
                .filter(hr -> hr.getDiseaseType() != null && !hr.getDiseaseType().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        HealthRecord::getDiseaseType,
                        Collectors.counting()));

        return diseaseCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    @Override
    public Map<String, Long> getTopDiseaseTypesByDoctorSortedByQuarter(int doctorId, int year)
            throws ParseException {
        Session s = this.factory.getObject().getCurrentSession();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-01-01");
        Date toDate = sdf.parse((year + 1) + "-01-01");

        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("appointment");
        q.distinct(true);

        q.where(
                b.and(
                        b.equal(root.get("appointment").get("doctor").get("id"), doctorId),
                        b.between(root.get("createdDate"), fromDate, toDate),
                        b.isNotNull(root.get("diseaseType")),
                        b.notEqual(root.get("diseaseType"), "")));

        List<HealthRecord> records = s.createQuery(q).getResultList();

        Map<String, Long> diseaseCountByQuarter = new HashMap<>();

        for (HealthRecord record : records) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(record.getCreatedDate());
            int month = cal.get(Calendar.MONTH); 
            int quarter = (month / 3) + 1; 

            String diseaseWithQuarter = record.getDiseaseType();

            diseaseCountByQuarter.put(
                    diseaseWithQuarter,
                    diseaseCountByQuarter.getOrDefault(diseaseWithQuarter, 0L) + 1);
        }

        return diseaseCountByQuarter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }
    
}
