    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 *
 * @author Giidavibe
 */

// Marks this class as a Spring configuration class
@Configuration
public class ThymeleafConfig {

    // Configures and returns a template resolver for Thymeleaf
    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        // Creates a new ClassLoaderTemplateResolver to locate and resolve Thymeleaf templates
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // Sets the prefix for template file locations, pointing to the "templates/" directory
        templateResolver.setPrefix("templates/");
        // Sets the suffix for template files, ensuring they end with ".html"
        templateResolver.setSuffix(".html");
        // Specifies the template mode as HTML for processing HTML files
        templateResolver.setTemplateMode("HTML");
        // Sets the character encoding to UTF-8 for proper handling of special characters
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    // Configures and returns a Thymeleaf template engine
    @Bean
    public SpringTemplateEngine templateEngine() {
        // Creates a new SpringTemplateEngine for processing Thymeleaf templates
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        // Associates the template resolver with the engine to locate templates
        templateEngine.setTemplateResolver(templateResolver());
        // Adds the Spring Security dialect for integrating Thymeleaf with Spring Security features
        templateEngine.addDialect(new SpringSecurityDialect());
        return templateEngine;
    }

    // Configures and returns a view resolver for Thymeleaf
    @Bean
    public ViewResolver thymeleafViewResolver() {
        // Creates a new ThymeleafViewResolver to resolve views for Thymeleaf templates
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        // Associates the template engine with the view resolver for processing templates
        viewResolver.setTemplateEngine(templateEngine());
        // Sets the character encoding to UTF-8 for consistent rendering of special characters
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }
}
