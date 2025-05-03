package com.can.repositories.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;  
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import com.can.pojo.Rating;
import com.can.repositories.RatingRepository;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
    public List<Rating> getAllRatings( Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo doctor_id
            String doctorId = params.get("doctorId");
            if (doctorId != null) {
                predicates.add(builder.equal(root.get("doctor").get("id"), Integer.parseInt(doctorId)));
            }

            // Lọc theo patient_id
            String patientId = params.get("patientId");
            if (patientId != null) {
                predicates.add(builder.equal(root.get("patient").get("id"), Integer.parseInt(patientId)));
            }

            // Áp dụng các điều kiện lọc
            query.where(predicates.toArray(new Predicate[0]));
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
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Rating.class, id);
    }

    @Override
    public List<Rating> getRatingsByDoctorId(Integer doctorId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);
        
        query.where(builder.equal(root.get("doctor").get("id"), doctorId));
        Query hqlQuery = session.createQuery(query);
        return hqlQuery.getResultList();
    }

    @Override
    public List<Rating> getRatingsByPatientId(Integer patientId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Rating> query = builder.createQuery(Rating.class);
        Root<Rating> root = query.from(Rating.class);
        
        query.where(builder.equal(root.get("patient").get("id"), patientId));
        Query hqlQuery = session.createQuery(query);
        return hqlQuery.getResultList();
    }

    @Override
    public Rating addRating(Rating rating) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.save(rating);
        transaction.commit();
        return rating;
    }

    @Override
    public Rating updateRating(Rating rating) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.update(rating);
        transaction.commit();
        return rating;
    }

    @Override
    public void deleteRating(Integer ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        Rating rating = session.get(Rating.class, ratingId);
        if (rating != null) {
            session.delete(rating);
        }
        transaction.commit();
    }

    @Override
    public boolean isRatingExist(int ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        Rating rating = session.get(Rating.class, ratingId);
        return rating != null;
    }
    
}
