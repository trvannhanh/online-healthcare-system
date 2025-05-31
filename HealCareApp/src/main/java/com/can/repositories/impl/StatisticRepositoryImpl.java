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

        // Lọc theo bác sĩ và đếm số bệnh nhân duy nhất
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

        // Tính ngày cuối cùng của tháng
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();
        // Tận dụng phương thức countDistinctPatientsByDoctorAndDateRange
        return countDistinctPatientsByDoctorAndDateRange(doctorId, fromDate, toDate);
    }

    @Override
    public Integer countDistinctPatientsByDoctorAndQuarter(int doctorId, int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1; // Tháng bắt đầu của quý
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        // Tính ngày cuối cùng của quý
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2); // Thêm 2 tháng để đến tháng cuối của quý
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        // Tận dụng phương thức countDistinctPatientsByDoctorAndDateRange
        return countDistinctPatientsByDoctorAndDateRange(doctorId, fromDate, toDate);
    }

    @Override
    public Integer countDistinctPatientsByDateRange(Date fromDateStr, Date toDateStr) throws ParseException {
        List<Appointment> appointments = this.getAppointmentsCompleteByDateRange(fromDateStr, toDateStr);

        return (int) appointments.stream()
                .map(Appointment::getPatient)
                .map(Patient::getId)
                .distinct()
                .count();
    }

    @Override
    public Integer countDistinctPatientsByQuarter(int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1; // Tháng bắt đầu của quý
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        // Tính ngày cuối cùng của quý
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2); // Thêm 2 tháng để đến tháng cuối của quý
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return countDistinctPatientsByDateRange(fromDate, toDate);
    }

    @Override
    public Integer countDistinctPatientsByMonth(int year, int month) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        // Tính ngày cuối cùng của tháng
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        return countDistinctPatientsByDateRange(fromDate, toDate);
    }

    @Override
    public List<Integer> getMonthlyStatistics(int year) throws ParseException {
        try {
            List<Integer> monthlyData = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                monthlyData.add(countDistinctPatientsByMonth(year, i));
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
                quarterlyData.add(countDistinctPatientsByQuarter(year, i));
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
        // Lấy danh sách payment hoàn thành trong khoảng thời gian
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        // Sử dụng stream để tính tổng doanh thu
        Double totalRevenue = payments.stream()
                .mapToDouble(payment -> payment.getAmount())
                .sum();

        return totalRevenue;
    }

    @Override
    public Double getRevenueByQuarter(int year, int quarter) throws ParseException {
        int startMonth = (quarter - 1) * 3 + 1; // First month of quarter (1, 4, 7, 10)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        // Calculate last day of the quarter
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2); // Add 2 months to get to last month of quarter
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        // Lấy danh sách payment hoàn thành trong khoảng thời gian
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        // Calculate total revenue
        Double totalRevenue = payments.stream()
                .mapToDouble(payment -> payment.getAmount())
                .sum();

        return totalRevenue;
    }

    @Override
    public Double getRevenueByMonth(int year, int month) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        // Calculate last day of the month
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
        // Lấy tất cả các thanh toán đã hoàn thành trong khoảng thời gian
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        // Khởi tạo map kết quả
        Map<String, Double> result = new HashMap<>();

        // Nhóm các thanh toán theo phương thức thanh toán và tính tổng
        for (PaymentMethod method : PaymentMethod.values()) {
            String methodName = method.toString();

            // Tính tổng doanh thu cho phương thức thanh toán này
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
        // Chuyển đổi tháng sang khoảng thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + month + "-01");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        // Tận dụng phương thức đã có
        return getRevenueByPaymentMethodAndDateRange(fromDate, toDate);
    }

    @Override
    public Map<String, Double> getRevenueByPaymentMethodAndQuarter(int year, int quarter) throws ParseException {
        // Tính ngày đầu quý
        int startMonth = (quarter - 1) * 3 + 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

        // Tính ngày cuối quý
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        calendar.add(Calendar.MONTH, 2);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date toDate = calendar.getTime();

        // Tận dụng phương thức đã có
        return getRevenueByPaymentMethodAndDateRange(fromDate, toDate);
    }

    @Override
    public Map<String, Map<String, Object>> getPaymentMethodStats(Date fromDate, Date toDate) throws ParseException {
        // Lấy tất cả các thanh toán đã hoàn thành trong khoảng thời gian
        List<Payment> payments = getPaymentCompleteByDateRange(fromDate, toDate);

        // Khởi tạo map kết quả
        Map<String, Map<String, Object>> result = new HashMap<>();

        // Tính toán thống kê cho từng phương thức thanh toán
        for (PaymentMethod method : PaymentMethod.values()) {
            String methodName = method.toString();

            // Lọc thanh toán theo phương thức
            List<Payment> methodPayments = payments.stream()
                    .filter(p -> p.getPaymentMethod() == method)
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("count", methodPayments.size()); // Số lượng giao dịch

            if (methodPayments.isEmpty()) {
                stats.put("total", 0.0);
                stats.put("average", 0.0);
                stats.put("min", 0.0);
                stats.put("max", 0.0);
            } else {
                // Tính các giá trị thống kê
                DoubleSummaryStatistics summaryStatistics = methodPayments.stream()
                        .mapToDouble(Payment::getAmount)
                        .summaryStatistics();

                stats.put("total", summaryStatistics.getSum()); // Tổng doanh thu
                stats.put("average", summaryStatistics.getAverage()); // Trung bình
                stats.put("min", summaryStatistics.getMin()); // Giá trị thấp nhất
                stats.put("max", summaryStatistics.getMax()); // Giá trị cao nhất
            }

            result.put(methodName, stats);
        }

        return result;
    }

    @Override
    public Map<Integer, Map<String, Double>> getMonthlyRevenueByPaymentMethod(int year) throws ParseException {
        Map<Integer, Map<String, Double>> result = new HashMap<>();

        // Tính toán cho từng tháng trong năm
        for (int month = 1; month <= 12; month++) {
            // Tận dụng phương thức đã có để lấy doanh thu theo phương thức thanh toán cho
            // tháng này
            Map<String, Double> monthRevenue = getRevenueByPaymentMethodAndMonth(year, month);
            result.put(month, monthRevenue);
        }

        return result;
    }

    // @Override
    // public Map<String, Long> getTopDiseaseTypes(Date fromDate, Date toDate, int topN) throws ParseException {
    //     Session s = this.factory.getObject().getCurrentSession();

    //     // Lấy danh sách health records trong khoảng thời gian
    //     CriteriaBuilder b = s.getCriteriaBuilder();
    //     CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
    //     Root<HealthRecord> root = q.from(HealthRecord.class);

    //     // Thêm điều kiện thời gian và disease_type không null
    //     q.where(
    //             b.and(
    //                     b.between(root.get("createdDate"), fromDate, toDate),
    //                     b.isNotNull(root.get("diseaseType")),
    //                     b.notEqual(root.get("diseaseType"), "")));

    //     // Thực thi truy vấn để lấy danh sách health records
    //     List<HealthRecord> records = s.createQuery(q).getResultList();

    //     // Sử dụng Java Stream để nhóm và đếm loại bệnh
    //     Map<String, Long> diseaseCount = records.stream()
    //             .filter(hr -> hr.getDiseaseType() != null && !hr.getDiseaseType().trim().isEmpty())
    //             .collect(Collectors.groupingBy(
    //                     HealthRecord::getDiseaseType,
    //                     Collectors.counting()));

    //     // Sắp xếp theo số lượng giảm dần và giới hạn số lượng kết quả
    //     return diseaseCount.entrySet().stream()
    //             .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    //             .limit(topN)
    //             .collect(Collectors.toMap(
    //                     Map.Entry::getKey,
    //                     Map.Entry::getValue,
    //                     (e1, e2) -> e1,
    //                     LinkedHashMap::new));
    // }

    // @Override
    // public Map<Integer, Map<String, Long>> getMostCommonDiseaseByMonth(int year) throws ParseException {
    //     Map<Integer, Map<String, Long>> result = new HashMap<>();

    //     // Tính toán cho từng tháng trong năm
    //     for (int month = 1; month <= 12; month++) {
    //         // Tạo khoảng thời gian cho tháng
    //         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    //         Date fromDate = sdf.parse(year + "-" + month + "-01");

    //         // Tính ngày cuối cùng của tháng
    //         Calendar calendar = Calendar.getInstance();
    //         calendar.setTime(fromDate);
    //         calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    //         Date toDate = calendar.getTime();

    //         // Lấy bệnh phổ biến nhất trong tháng này
    //         Map<String, Long> topDisease = getTopDiseaseTypes(fromDate, toDate, 1);
    //         result.put(month, topDisease);
    //     }

    //     return result;
    // }

    // @Override
    // public Map<Integer, Map<String, Long>> getMostCommonDiseaseByQuarter(int year) throws ParseException {
    //     Map<Integer, Map<String, Long>> result = new HashMap<>();

    //     // Tính toán cho từng quý trong năm
    //     for (int quarter = 1; quarter <= 4; quarter++) {
    //         int startMonth = (quarter - 1) * 3 + 1; // Tháng bắt đầu của quý
    //         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    //         Date fromDate = sdf.parse(year + "-" + startMonth + "-01");

    //         // Tính ngày cuối cùng của quý
    //         Calendar calendar = Calendar.getInstance();
    //         calendar.setTime(fromDate);
    //         calendar.add(Calendar.MONTH, 2); // Thêm 2 tháng để đến tháng cuối của quý
    //         calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    //         Date toDate = calendar.getTime();

    //         // Lấy bệnh phổ biến nhất trong quý này
    //         Map<String, Long> topDisease = getTopDiseaseTypes(fromDate, toDate, 1);
    //         result.put(quarter, topDisease);
    //     }

    //     return result;
    // }

    @Override
    public Map<String, Long> getTopDiseaseTypesByDoctorSortedByMonth(int doctorId, int year)
            throws ParseException {
        Session s = this.factory.getObject().getCurrentSession();

        // Tạo khoảng thời gian cho cả năm
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-01-01");
        Date toDate = sdf.parse((year + 1) + "-01-01");

        // Sử dụng Criteria API
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("appointment");
        q.distinct(true);

        // Điều kiện: doctorId, khoảng thời gian, và diseaseType không null
        q.where(
                b.and(
                        b.equal(root.get("appointment").get("doctor").get("id"), doctorId),
                        b.between(root.get("createdDate"), fromDate, toDate),
                        b.isNotNull(root.get("diseaseType")),
                        b.notEqual(root.get("diseaseType"), "")));

        // Thực thi truy vấn để lấy danh sách health records
        List<HealthRecord> records = s.createQuery(q).getResultList();

        // Sử dụng Java Stream để nhóm và đếm loại bệnh
        Map<String, Long> diseaseCount = records.stream()
                .filter(hr -> hr.getDiseaseType() != null && !hr.getDiseaseType().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        HealthRecord::getDiseaseType,
                        Collectors.counting()));

        // Sắp xếp theo số lượng giảm dần
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

        // Tạo khoảng thời gian cho cả năm
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(year + "-01-01");
        Date toDate = sdf.parse((year + 1) + "-01-01");

        // Sử dụng Criteria API
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> q = b.createQuery(HealthRecord.class);
        Root<HealthRecord> root = q.from(HealthRecord.class);

        root.fetch("appointment");
        q.distinct(true);

        // Điều kiện: doctorId, khoảng thời gian, và diseaseType không null
        q.where(
                b.and(
                        b.equal(root.get("appointment").get("doctor").get("id"), doctorId),
                        b.between(root.get("createdDate"), fromDate, toDate),
                        b.isNotNull(root.get("diseaseType")),
                        b.notEqual(root.get("diseaseType"), "")));

        // Thực thi truy vấn để lấy danh sách health records
        List<HealthRecord> records = s.createQuery(q).getResultList();

        // Nhóm records theo quý và sau đó thống kê
        Map<String, Long> diseaseCountByQuarter = new HashMap<>();

        for (HealthRecord record : records) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(record.getCreatedDate());
            int month = cal.get(Calendar.MONTH); // 0-11
            int quarter = (month / 3) + 1; // 1-4

            // Thêm thông tin quý vào tên bệnh để phân biệt
            String diseaseWithQuarter = record.getDiseaseType();

            // Cập nhật số lượng
            diseaseCountByQuarter.put(
                    diseaseWithQuarter,
                    diseaseCountByQuarter.getOrDefault(diseaseWithQuarter, 0L) + 1);
        }

        // Sắp xếp theo số lượng giảm dần
        return diseaseCountByQuarter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }
}
