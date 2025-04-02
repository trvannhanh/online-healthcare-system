/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.healcarehibernate;

import com.can.pojo.Appointment;
import com.can.pojo.Doctor;
import com.can.pojo.Heath_Records;
import com.can.pojo.Notifications;
import com.can.pojo.Patient;
import com.can.pojo.Payment;
import com.can.pojo.Rating;
import com.can.pojo.User;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

/**
 *
 * @author Giidavibe
 */
public class HibernateUtils {
    private static final SessionFactory FACTORY;
    
    static {
        Configuration conf = new Configuration();
        Properties props = new Properties();
        props.setProperty(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        props.setProperty(Environment.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
        props.setProperty(Environment.JAKARTA_JDBC_URL, "jdbc:mysql://localhost/healcaredb");
        props.setProperty(Environment.JAKARTA_JDBC_USER, "root");
        props.setProperty(Environment.JAKARTA_JDBC_PASSWORD, "Admin@123");
        props.setProperty(Environment.SHOW_SQL, "true");
        
        conf.setProperties(props);
        
        conf.addAnnotatedClass(User.class);
        conf.addAnnotatedClass(Doctor.class);
        conf.addAnnotatedClass(Patient.class);
        conf.addAnnotatedClass(Appointment.class);
        conf.addAnnotatedClass(Heath_Records.class);
        conf.addAnnotatedClass(Rating.class);
        conf.addAnnotatedClass(Notifications.class);
        conf.addAnnotatedClass(Payment.class);
        
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(conf.getProperties()).build();
        
        FACTORY = conf.buildSessionFactory(serviceRegistry);
    }

    /**
     * @return the FACTORY
     */
    public static SessionFactory getFACTORY() {
        return FACTORY;
    }
}
