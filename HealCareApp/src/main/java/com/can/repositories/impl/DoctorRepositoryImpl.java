/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;


import com.can.pojo.Doctor;
import com.can.pojo.User;
import com.can.repositories.DoctorRepository;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Giidavibe
 */
@Repository
@Transactional
public class DoctorRepositoryImpl implements DoctorRepository{
    @Autowired
    private LocalSessionFactoryBean factory;
    
    private static final int PAGE_SIZE = 10;
    
    public List<Doctor> getDoctors(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
            CriteriaBuilder b = s.getCriteriaBuilder();
            CriteriaQuery<Doctor> q = b.createQuery(Doctor.class);
            Root<Doctor> root = q.from(Doctor.class);

            Join<Doctor, User> userJoin = root.join("user");

            if (params != null) {
                List<Predicate> predicates = new ArrayList<>();

                //Lọc theo tên bác sĩ
                String doctorName = params.get("doctorName");
                if (doctorName != null && !doctorName.isEmpty()) {
                    String searchPattern = String.format("%%%s%%", doctorName.toLowerCase());
                    Predicate firstNamePredicate = b.like(
                            b.lower(userJoin.get("firstName")),
                            searchPattern
                    );
                    Predicate lastNamePredicate = b.like(
                            b.lower(userJoin.get("lastName")),
                            searchPattern
                    );
                    predicates.add(b.or(firstNamePredicate, lastNamePredicate));
                }

                //Lọc theo chuyên khoa
                String specialization = params.get("specialization");
                if (specialization != null && !specialization.isEmpty()) {
                    predicates.add(b.equal(
                            b.lower(root.get("specialization")),
                            specialization.toLowerCase()
                    ));
                }

                //Lọc theo bệnh viện
                String hospital = params.get("hospital");
                if (hospital != null && !hospital.isEmpty()) {
                    predicates.add(b.equal(
                            b.lower(root.get("hospital")),
                            hospital.toLowerCase()
                    ));
                }

                // Áp dụng các điều kiện lọc
                q.where(predicates.toArray(Predicate[]::new));
            }
            
            Query query = s.createQuery(q);
            
            if (params != null) {
                int page = Integer.parseInt(params.getOrDefault("page", "1"));
                int start = (page - 1) * PAGE_SIZE;
                
                query.setFirstResult(start);
                query.setMaxResults(PAGE_SIZE);
            }
            
            return query.getResultList();
        
        
    }
    
    public Doctor getDoctorById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
            return s.get(Doctor.class, id);
        
    }
    
    public List<Doctor> getAllDoctors(){
        return getDoctors(null);
    }
    
}
