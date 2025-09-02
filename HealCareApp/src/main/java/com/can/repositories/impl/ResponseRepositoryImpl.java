package com.can.repositories.impl;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import com.can.pojo.Rating;
import com.can.pojo.Response;
import java.util.List;
import java.util.Map;
import com.can.repositories.ResponseRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.Session;

@Repository
@Transactional
public class ResponseRepositoryImpl implements ResponseRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public Response addResponse(Response response) {
        Session session = this.factory.getObject().getCurrentSession();

        if (response.getRating() == null) {
            throw new RuntimeException("Rating is required");
        }
        Rating rating = session.get(Rating.class, response.getRating().getId());
        if (rating == null) {
            throw new RuntimeException("Rating with id " + response.getRating().getId() + " not found");
        }

        session.persist(response);
        return response;
    }
    
    @Override
    public Response updateResponse(Response response) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(response);
        return response;
    }

    @Override
    public void deleteResponse(Integer id) {
        Session session = this.factory.getObject().getCurrentSession();
        Response response = session.get(Response.class, id);
        if (response != null) {
            session.remove(response);
        }
    }

    @Override
    public boolean isResponseExist(Integer id) {
        Response response = this.getResponseById(id);
        return response != null;
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
            return null; 
        }
    }

    @Override
    public boolean isRatingResponsed(int ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Response> root = query.from(Response.class);

        query.select(builder.count(root));
        query.where(builder.equal(root.get("rating").get("id"), ratingId));

        return session.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public List<Response> getAllResponses(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        root.fetch("rating", JoinType.LEFT).fetch("appointment", JoinType.LEFT).fetch("doctor", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);
        root.fetch("rating", JoinType.LEFT).fetch("appointment", JoinType.LEFT).fetch("patient", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        if (params != null) {
            List<Predicate> predicates = new ArrayList<>();

            String doctorId = params.get("doctorId");
            if (doctorId != null) {
                predicates.add(builder.equal(root.get("rating").get("appointment").get("doctor").get("id"),
                        Integer.parseInt(doctorId)));
            }

            if (!predicates.isEmpty()) {
                query.where(predicates.toArray(new Predicate[0]));
            }
        }

        query.distinct(true);

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
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        root.fetch("rating", JoinType.LEFT).fetch("appointment", JoinType.LEFT).fetch("doctor", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);
        root.fetch("rating", JoinType.LEFT).fetch("appointment", JoinType.LEFT).fetch("patient", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        query.where(builder.equal(root.get("id"), id));
        query.distinct(true);

        try {
            return session.createQuery(query).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Response> getResponsesByDoctorId(Integer doctorId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        root.fetch("rating", JoinType.LEFT).fetch("appointment", JoinType.LEFT).fetch("doctor", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);
        root.fetch("rating", JoinType.LEFT).fetch("appointment", JoinType.LEFT).fetch("patient", JoinType.LEFT)
                .fetch("user", JoinType.LEFT);

        query.where(builder.equal(root.get("rating").get("appointment").get("doctor").get("id"), doctorId));
        query.distinct(true);

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    

    @Override
    public Response getResponseByRatingId(Integer ratingId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Response> query = builder.createQuery(Response.class);
        Root<Response> root = query.from(Response.class);

        root.fetch("rating", JoinType.INNER);

        query.where(builder.equal(root.get("rating").get("id"), ratingId));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null; 
        }
    }

}
