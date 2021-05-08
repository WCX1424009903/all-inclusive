package com.example.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) throws BeansException {
        return (T)applicationContext.getBean(beanName);
    }

    public static  <T> T getBean(Class<T> c) throws BeansException {
        return (T)applicationContext.getBean(c);
    }
}
