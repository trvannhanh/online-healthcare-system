package com.can.repositories.impl;

import com.can.pojo.PatientSelfReport;
import com.can.repositories.PatientSelfReportRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PatientSelfReportRepositoryImpl implements PatientSelfReportRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    
    @Override
    public PatientSelfReport addPatientSelfReport(PatientSelfReport patientSelfReport) {
        Session s = this.factory.getObject().getCurrentSession();
        
        if (existsByPatientId(patientSelfReport.getPatient().getId())) {
            throw new RuntimeException("Bệnh nhân đã có hồ sơ sức khỏe");
        }
        
        s.persist(patientSelfReport);
        return patientSelfReport;
    }

    @Override
    public PatientSelfReport updatePatientSelfReport(PatientSelfReport patientSelfReport) {
        Session s = this.factory.getObject().getCurrentSession();
        
        PatientSelfReport existingReport = getPatientSelfReportById(patientSelfReport.getId());
        if (existingReport == null) {
            throw new RuntimeException("Không tìm thấy hồ sơ sức khỏe bệnh nhân");
        }
        
        return (PatientSelfReport) s.merge(patientSelfReport);
    }

    
    @Override
    public PatientSelfReport getPatientSelfReportById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(PatientSelfReport.class, id);
    }

    @Override
    public PatientSelfReport getPatientSelfReportByPatientId(int patientId, String username) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<PatientSelfReport> q = b.createQuery(PatientSelfReport.class);
        Root<PatientSelfReport> root = q.from(PatientSelfReport.class);
        
        q.where(b.equal(root.get("patient").get("id"), patientId));
        
        Query<PatientSelfReport> query = s.createQuery(q);
        
        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    

    @Override
    public boolean existsByPatientId(int patientId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<PatientSelfReport> root = q.from(PatientSelfReport.class);
        
        q.select(b.count(root));
        q.where(b.equal(root.get("patient").get("id"), patientId));
        
        return s.createQuery(q).getSingleResult() > 0;
    }
}