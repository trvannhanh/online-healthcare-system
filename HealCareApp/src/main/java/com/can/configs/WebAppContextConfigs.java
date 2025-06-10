/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author Giidavibe
 */
@Configuration 
@EnableWebMvc 
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.can.controllers",
    "com.can.repositories",
    "com.can.services"
})
public class WebAppContextConfigs implements WebMvcConfigurer { 

    @Override 
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // Enables the default servlet to handle static resources and unmapped requests
        configurer.enable();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Maps requests for "/js/**" to JavaScript files in "classpath:/static/js"
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js");
    }
}
