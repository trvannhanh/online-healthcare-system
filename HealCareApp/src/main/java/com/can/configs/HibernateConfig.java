/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import java.util.Properties;
import javax.sql.DataSource;
import static org.hibernate.cfg.JdbcSettings.DIALECT;
import static org.hibernate.cfg.JdbcSettings.SHOW_SQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 *
 * @author Giidavibe
 */
// Marks this class as a Spring configuration class
@Configuration
// Loads properties from the 'healcaredb.properties' file in the classpath
@PropertySource("classpath:healcaredb.properties")
public class HibernateConfig {

    // Injects the Environment object to access properties from the configuration file
    @Autowired
    private Environment env;

    // Configures and returns a Hibernate SessionFactory bean
    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        // Creates a new LocalSessionFactoryBean for Hibernate
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        // Specifies the package to scan for Hibernate entity classes
        sessionFactory.setPackagesToScan(new String[]{
            "com.can.pojo"
        });
        // Sets the data source for database connectivity
        sessionFactory.setDataSource(dataSource());
        // Applies Hibernate properties like dialect and SQL logging
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    // Configures and returns a DataSource bean for database connection
    @Bean
    public DataSource dataSource() {
        // Creates a new DriverManagerDataSource for JDBC connectivity
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        // Sets the JDBC driver class from the properties file
        dataSource.setDriverClassName(env.getProperty("hibernate.connection.driverClass"));
        // Sets the database URL from the properties file
        dataSource.setUrl(env.getProperty("hibernate.connection.url"));
        // Sets the database username from the properties file
        dataSource.setUsername(env.getProperty("hibernate.connection.username"));
        // Sets the database password from the properties file
        dataSource.setPassword(env.getProperty("hibernate.connection.password"));
        return dataSource;
    }

    // Configures Hibernate properties such as dialect and SQL logging
    private Properties hibernateProperties() {
        // Creates a new Properties object to hold Hibernate settings
        Properties props = new Properties();
        // Sets the Hibernate dialect to match the database type, loaded from properties file
        props.put(DIALECT, env.getProperty("hibernate.dialect"));
        // Enables or disables SQL query logging, based on the properties file
        props.put(SHOW_SQL, env.getProperty("hibernate.showSql"));
        return props;
    }

    // Configures and returns a Hibernate TransactionManager bean
    @Bean
    public HibernateTransactionManager transactionManager() {
        // Creates a new HibernateTransactionManager for managing transactions
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        // Associates the transaction manager with the SessionFactory
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }
}
