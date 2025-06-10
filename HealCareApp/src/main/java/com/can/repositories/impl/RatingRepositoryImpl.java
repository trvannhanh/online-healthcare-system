package com.can.repositories.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;
    
    @Override
    public Rating addRating(Rating rating) {
        Session s = this.factory.getObject().getCurrentSession();
        s.persist(rating);
        return rating;
    }
    
    @Override
    public Rating updateRating(Rating rating) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(rating);
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

        query.select(builder.avg(root.get("rating")));
        query.where(builder.equal(root.get("appointment").get("doctor").get("id"), doctorId));
        try {
            Double average = session.createQuery(query).getSingleResult();
            // Returns 0.0 if no ratings are found for the doctor
            return average != null ? average : 0.0; 
        } catch (Exception e) {
            // Logs error when calculating average rating for doctor
            System.err.println("Error calculating average rating for doctor ID " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0; 
        }
    }

    @Override
    public boolean isAppointmentRated(int appointmentId) {
        // Checks if a rating exists for the given appointment
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Rating> root = query.from(Rating.class);

        query.select(builder.count(root));
        query.where(builder.equal(root.get("appointment").get("id"), appointmentId));

        return session.createQuery(query).getSingleResult() > 0;
    }
    
    
    @Override
    public List<Rating> getAllRatings(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        root.fetch("appointment", JoinType.INNER);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            String doctorId = params.get("doctorId");
            if (doctorId != null) {
                predicates.add(
                        builder.equal(root.get("appointment").get("doctor").get("id"), Integer.parseInt(doctorId)));
            }

            String patientId = params.get("patientId");
            if (patientId != null) {
                predicates.add(
                        builder.equal(root.get("appointment").get("patient").get("id"), Integer.parseInt(patientId)));
            }

            String appointmentId = params.get("appointmentId");
            if (appointmentId != null) {
                predicates.add(builder.equal(root.get("appointment").get("id"), Integer.parseInt(appointmentId)));
            }

            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }
        }

        Query hqlQuery = session.createQuery(query);

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
            // Returns null if no rating is found for the given ID
            return null;
        }
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

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

        root.fetch("appointment", JoinType.INNER);

        query.where(builder.equal(root.get("appointment").get("patient").get("id"), patientId));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Rating getRatingByAppointmentId(Integer appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        root.fetch("appointment", JoinType.INNER);

        query.where(builder.equal(root.get("appointment").get("id"), appointmentId));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            // Returns null if no rating is found for the appointment
            return null; 
        }
    }
}