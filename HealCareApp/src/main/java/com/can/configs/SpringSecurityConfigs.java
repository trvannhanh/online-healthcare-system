/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import com.can.pojo.User;
import com.can.services.DoctorService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 *
 * @author Giidavibe
 */
@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.can.controllers",
    "com.can.repositories",
    "com.can.services"
})
public class SpringSecurityConfigs {

    @Autowired
    private UserDetailsService userDetailService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public Cloudinary cloudinary() {
        Cloudinary cloudinary
                = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "dqpkxxzaf",
                        "api_key", "948716666497639",
                        "api_secret", "VxK3FJQ_0HFa4UeMXYI0nfSIGko",
                        "secure", true));

        return cloudinary;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
            Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(c -> c.disable()).authorizeHttpRequests(requests -> requests
                .requestMatchers("/", "/home").authenticated()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/api/payment/**").permitAll()
                .requestMatchers("/api/admin/verify-doctor/**").hasRole("ADMIN")
                .requestMatchers("/appointments/**").hasRole("ADMIN")
                .requestMatchers("/statistics/**").hasRole("ADMIN")
                .requestMatchers("/notifications/**").hasRole("ADMIN")
                .requestMatchers("/api/secure/appointments/**").hasRole("PATIENT")
                .requestMatchers("/api/**").access(new DoctorAuthorizationManager())
                .anyRequest().authenticated()
        )
                .formLogin(form -> form.loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login").permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); 
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    
    
    public class DoctorAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

        @Override
        public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
            Authentication authentication = authenticationSupplier.get();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new AuthorizationDecision(false);
            }

            boolean hasDoctorRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DOCTOR"));
            if (!hasDoctorRole) {
                return new AuthorizationDecision(false);
            }

            String username = authentication.getName();
            User user = (User) userDetailService.loadUserByUsername(username);
            if (user == null) {
                return new AuthorizationDecision(false);
            }

            boolean isVerified = doctorService.isDoctorVerified(user.getId());
            return new AuthorizationDecision(isVerified);
        }
    }
        
}
