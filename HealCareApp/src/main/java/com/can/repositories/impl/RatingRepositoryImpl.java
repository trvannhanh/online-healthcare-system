package com.can.repositories.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.can.pojo.Appointment;
import com.can.pojo.Doctor;
import com.can.pojo.Patient;
import com.can.pojo.Rating;
import com.can.repositories.RatingRepository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;

/**
 *
 * @author DELL
 */
@Repository
@Transactional
public class RatingRepositoryImpl implements RatingRepository {
    // Implement the methods defined in the RatingRepository interface here
    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Rating> getAllRatings(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        // Mặc định fetch appointment để tránh lazy loading exception
        root.fetch("appointment", JoinType.INNER);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo doctor_id - thông qua appointment
            String doctorId = params.get("doctorId");
            if (doctorId != null) {
                predicates.add(
                        builder.equal(root.get("appointment").get("doctor").get("id"), Integer.parseInt(doctorId)));
            }

            // Lọc theo patient_id - thông qua appointment
            String patientId = params.get("patientId");
            if (patientId != null) {
                predicates.add(
                        builder.equal(root.get("appointment").get("patient").get("id"), Integer.parseInt(patientId)));
            }

            // Lọc theo appointment_id
            String appointmentId = params.get("appointmentId");
            if (appointmentId != null) {
                predicates.add(builder.equal(root.get("appointment").get("id"), Integer.parseInt(appointmentId)));
            }

            // Áp dụng các điều kiện lọc
            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }
        }

        Query hqlQuery = session.createQuery(query);

        // Phân trang nếu có
        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;
            hqlQuery.setFirstResult(start);
            hqlQuery.setMaxResults(PAGE_SIZE);
        }

        return hqlQuery.getResultList();
    }

    @Override
    public Rating getRatingById(Integer id) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Rating> q = b.createQuery(Rating.class);
        Root<Rating> root = q.from(Rating.class);

        // Fetch appointment và các mối quan hệ liên quan
        root.fetch("appointment", JoinType.LEFT)
                .fetch("doctor", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        root.fetch("appointment", JoinType.LEFT)
                .fetch("patient", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        q.where(b.equal(root.get("id"), id));

        try {
            return s.createQuery(q).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        // Join với appointment để lấy thông tin doctor
        root.fetch("appointment", JoinType.INNER);
        query.where(builder.equal(root.get("appointment").get("doctor").get("id"), doctorId));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Rating> getRatingsByPatientId(Integer patientId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        // Fetch appointment thay vì join để eager loading data
        root.fetch("appointment", JoinType.INNER);

        // Sử dụng điều kiện lọc qua path từ root thay vì từ join
        query.where(builder.equal(root.get("appointment").get("patient").get("id"), patientId));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Rating addRating(Rating rating) {
        Session s = this.factory.getObject().getCurrentSession();
        // Lưu rating vào cơ sở dữ liệu
        s.persist(rating);
        return rating;
    }

    @Override
    public Rating updateRating(Rating rating) {
        Session session = this.factory.getObject().getCurrentSession();
        session.update(rating);
        return rating;
    }

    @Override
    public void deleteRating(Integer ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        Rating rating = session.get(Rating.class, ratingId);
        if (rating != null) {
            session.remove(rating);
        }
    }

    @Override
    public boolean isRatingExist(int ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        Rating rating = session.get(Rating.class, ratingId);
        return rating != null;
    }

    @Override
    public Double getAverageRatingForDoctor(Integer doctorId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Rating> root = query.from(Rating.class);

        // Tính trung bình cột "score" cho bác sĩ có ID được truyền vào
        query.select(builder.avg(root.get("rating")));
        query.where(builder.equal(root.get("appointment").get("doctor").get("id"), doctorId));
        try {
            Double average = session.createQuery(query).getSingleResult();
            return average != null ? average : 0.0; // Trả về 0.0 nếu không có đánh giá
        } catch (Exception e) {
            System.err.println("Error calculating average rating for doctor ID " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0; // Trả về 0.0 nếu có lỗi
        }
    }

    @Override
    public boolean isAppointmentRated(int appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Rating> root = query.from(Rating.class);

        query.select(builder.count(root));
        query.where(builder.equal(root.get("appointment").get("id"), appointmentId));

        return session.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public Rating getRatingByAppointmentId(Integer appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        // Fetch appointment để tránh lazy loading exception
        root.fetch("appointment", JoinType.INNER);

        query.where(builder.equal(root.get("appointment").get("id"), appointmentId));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null; // Trả về null nếu không tìm thấy đánh giá
        }
    }
}
