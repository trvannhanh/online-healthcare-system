package com.can.repositories.impl;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import com.can.pojo.Response;
import java.util.List;
import java.util.Map;

import com.can.repositories.ResponseRepository;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.Query;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import org.hibernate.Session;

@Repository
@Transactional
public class ResponseRepositoryImpl implements ResponseRepository {
        // Implement the methods defined in the RatingRepository interface here

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public List<Response> getAllResponses(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo doctorId
            String doctorId = params.get("doctorId");
            if (doctorId != null) {
                predicates.add(builder.equal(root.get("rating").get("doctor").get("id"), Integer.parseInt(doctorId)));
            }

            query.where(predicates.toArray(new Predicate[0]));
        }

        Query q = session.createQuery(query);

        if (params != null) {
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * PAGE_SIZE;
            q.setFirstResult(start);
            q.setMaxResults(PAGE_SIZE);
        }

        return q.getResultList();
    }

    @Override
    public Response getResponseById(Integer id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Response.class, id);
    }

    @Override
    public List<Response> getResponsesByDoctorId(Integer doctorId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        query.where(builder.equal(root.get("rating").get("doctor").get("id"), doctorId));
        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public Response addResponse(Response response) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.save(response);
        tx.commit();
        return response;
    }

    @Override
    public Response updateResponse(Response response) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.update(response);
        tx.commit();
        return response;
    }

    @Override
    public void deleteResponse(Integer responseId) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction tx = session.beginTransaction();
        Response response = session.get(Response.class, responseId);
        if (response != null) {
            session.delete(response);
        }
        tx.commit();
    }

    @Override
    public boolean isResponseExist(Integer responseId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Response.class, responseId) != null;
    }

    @Override
    public Response getResponsesByRating(Integer ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        query.where(builder.equal(root.get("rating").get("id"), ratingId));
        Query q = session.createQuery(query);
        try {
            return (Response) q.getSingleResult();
        } catch (Exception e) {
            return null; // Trả về null nếu không tìm thấy Response
        }
    }
    
}
