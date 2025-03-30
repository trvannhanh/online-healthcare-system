///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.can.healcarehibernate;
//
//import java.util.Properties;
//import org.hibernate.SessionFactory;
//import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
//import org.hibernate.cfg.Configuration;
//import org.hibernate.cfg.Environment;
//import org.hibernate.service.ServiceRegistry;
//
///**
// *
// * @author Giidavibe
// */
//public class HibernateUtils {
//    private static final SessionFactory FACTORY;
//    
//    static {
//        Configuration conf = new Configuration();
//        Properties props = new Properties();
//        props.setProperty(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
//        props.setProperty(Environment.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
//        props.setProperty(Environment.JAKARTA_JDBC_URL, "jdbc:mysql://localhost/saledb");
//        props.setProperty(Environment.JAKARTA_JDBC_USER, "root");
//        props.setProperty(Environment.JAKARTA_JDBC_PASSWORD, "root");
//        props.setProperty(Environment.SHOW_SQL, "true");
//        
//        conf.setProperties(props);
//        
////        conf.addAnnotatedClass(Category.class);
////        conf.addAnnotatedClass(Product.class);
////        conf.addAnnotatedClass(SaleOrder.class);
////        conf.addAnnotatedClass(Tag.class);
////        conf.addAnnotatedClass(User.class);
////        conf.addAnnotatedClass(Comment.class);
////        conf.addAnnotatedClass(OrderDetail.class);
////        conf.addAnnotatedClass(ProdTag.class);
//        
//        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(conf.getProperties()).build();
//        
//        FACTORY = conf.buildSessionFactory(serviceRegistry);
//    }
//
//    /**
//     * @return the FACTORY
//     */
//    public static SessionFactory getFACTORY() {
//        return FACTORY;
//    }
//}
