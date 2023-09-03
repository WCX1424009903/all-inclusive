package org.example.sharding.config;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 德鲁伊数据库连接池监控配置
 * @author wcx
 * @date 2021/8/17 20:36
 */
@Configuration
public class DruidConfig {

    /**
     * 配置Druid监控
     * 后台管理Servlet
     * @return
     */
//    @Bean
//    public ServletRegistrationBean statViewServlet(){
//        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
//        // 这是配置的druid监控的登录密码
//        Map<String,String> initParams = new HashMap<>(16);
//        initParams.put("loginUsername","root");
//        initParams.put("loginPassword","root");
//        // 默认就是允许所有访问
//        initParams.put("allow","");
//        // deny为黑名单ip配置
//        initParams.put("deny","192.168.15.21");
//        // 黑名单的IP
//        bean.setInitParameters(initParams);
//        return bean;
//    }
    /**
     * 配置web监控的filter
     * @return
     */
//    @Bean
//    public FilterRegistrationBean webStatFilter(){
//        FilterRegistrationBean bean = new FilterRegistrationBean();
//        bean.setFilter((Filter) new WebStatFilter());
//        Map<String,String> initParams = new HashMap<>(16);
//        // 过滤掉需要监控的文件
//        initParams.put("exclusions","/static/*,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
//        bean.setInitParameters(initParams);
//        bean.setUrlPatterns(Collections.singletonList("/*"));
//        return  bean;
//    }


}
