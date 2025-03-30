/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 *
 * @author Giidavibe
 */
// đầu não của hệ thống, làm gì cũng phải khai báo
public class DispatcherServletInit extends AbstractAnnotationConfigDispatcherServletInitializer{

    @Override // cấu hình chỉ @configuration không có kể thừa ai viết trong đây
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {
            ThymeleafConfig.class
        };
    }

    @Override // cấu hình mà implements configure viết trong đây
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {
            WebAppContextConfigs.class
        };
    }

    @Override // chỉ định kí hiệu ánh xạ
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
    
}
