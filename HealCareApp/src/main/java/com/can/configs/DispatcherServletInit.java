/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import com.can.filters.JwtFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 *
 * @author Giidavibe
 */
public class DispatcherServletInit extends AbstractAnnotationConfigDispatcherServletInitializer {

    // Specifies the root configuration classes for the application
    @Override 
    protected Class<?>[] getRootConfigClasses() {
        // Returns an array of configuration classes for setting up Thymeleaf, Hibernate, Spring Security, Firebase, and Email
        return new Class[] {
            ThymeleafConfig.class,
            HibernateConfig.class,
            SpringSecurityConfigs.class,
            FirebaseConfig.class,
            EmailConfig.class
        };
    }

    // Specifies the configuration classes for the DispatcherServlet
    @Override 
    protected Class<?>[] getServletConfigClasses() {
        // Returns the configuration class for web application context
        return new Class[] {
            WebAppContextConfigs.class
        };
    }

    // Defines the URL mappings for the DispatcherServlet
    @Override
    protected String[] getServletMappings() {
        // Maps the DispatcherServlet to handle all requests starting from the root path "/"
        return new String[] {"/"};
    }
    
    // Customizes the servlet registration, particularly for multipart file upload configuration
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // Defines the location for storing uploaded files
        String location = "/";
        // Sets the maximum file size for uploads to 5MB (5,242,880 bytes)
        long maxFileSize = 5242880; 
        // Sets the maximum request size to 20MB (20,971,520 bytes) for multipart requests
        long maxRequestSize = 20971520; 
        // Sets the file size threshold to 0, meaning files are written to disk immediately
        int fileSizeThreshold = 0;

        // Configures the multipart settings for file uploads with the specified parameters
        registration.setMultipartConfig(new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold));
    }
    
    // Specifies the filters to be applied to the DispatcherServlet
    @Override
    protected Filter[] getServletFilters() {
        // Returns an array containing the JwtFilter for processing JWT-based authentication
        return new Filter[] { new JwtFilter() }; 
    }
    
}