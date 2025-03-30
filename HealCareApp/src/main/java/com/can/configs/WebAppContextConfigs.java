/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author Giidavibe
 */
@Configuration //biến thằng này thành rổ đậu
@EnableWebMvc // hiện thực một số thứ trong WebMvcConfigurer( chuẩn của Spring) đã implements
@ComponentScan(basePackages = {
    "com.can.controllers"
})// chỉ định những nơi sử dụng annotaion
public class WebAppContextConfigs implements WebMvcConfigurer{ 

    @Override //kích hoạt Servlet
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
}
