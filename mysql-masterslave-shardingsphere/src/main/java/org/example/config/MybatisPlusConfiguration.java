package org.example.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis配置
 * @author WCX
 */

@Configuration
@MapperScan(basePackages = MybatisPlusConfiguration.MAPPER_FIRST)
public class MybatisPlusConfiguration {
    public static final String MAPPER_FIRST = "org.example.mapper";

    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor  = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDialect(new MySqlDialect());
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        return paginationInnerInterceptor;
    }

}
