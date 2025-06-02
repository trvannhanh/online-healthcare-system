/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories.impl;

import com.can.pojo.HealthRecord;
import com.can.repositories.HealthRecordRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author DELL
 */
@Repository
@Transactional
public class HealthRecordRepositoryImpl implements HealthRecordRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    
    
    @Override
    public HealthRecord createHealthRecord(HealthRecord healthRecord) {
        Session s = this.factory.getObject().getCurrentSession();
        s.persist(healthRecord);
        System.out.println("Kết quả khám được tạo với id: " + healthRecord.getId());
        return healthRecord;
    }

    @Override
    public HealthRecord getHealthRecordByAppointmentId(Integer appointmentId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<HealthRecord> query = builder.createQuery(HealthRecord.class);
        Root<HealthRecord> root = query.from(HealthRecord.class);
        query.select(root).where(builder.equal(root.get("appointment").get("id"), appointmentId));
        HealthRecord result = s.createQuery(query).uniqueResult();
        System.out.println("Truy vấn kết quả khám cho lịch hẹn: " + appointmentId + ", tìm thấy: " + (result != null));
        return result;
    }
    

}
