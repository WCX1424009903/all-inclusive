package org.example.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * mybatis多数据源配置
 * @author WCX
 */
@Configuration
@MapperScan(basePackages = MybatisPlusFirstConfiguration.MAPPER_FIRST,sqlSessionTemplateRef = "firstSqlSessionTemplate")
public class MybatisPlusFirstConfiguration {
    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    /**
     * mapper位置
     */
    public static final String MAPPER_FIRST = "org.example.mapper.first";
    /**
     * xml位置位置
     */
    public String[] mapperLocations = new String[]{
            "classpath*:/mapper/first/*.xml"
    };


    @Bean(name = "firstDataSource")
    @Primary
    @ConfigurationProperties("spring.datasource.first")
    public DataSource firstDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "firstSqlSessionFactory")
    public SqlSessionFactory firstSqlSessionFactory(@Qualifier("firstDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置xml位置
        bean.setMapperLocations(resolveMapperLocations());
        return bean.getObject();
    }

    @Bean(name = "firstDataSourceTransactionManager")
    public DataSourceTransactionManager firstDataSourceTransactionManager(@Qualifier("firstDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "firstSqlSessionTemplate")
    public SqlSessionTemplate firstSqlSessionTemplate(@Qualifier("firstSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    public Resource[] resolveMapperLocations() {
        return Stream.of(Optional.ofNullable(this.mapperLocations).orElse(new String[0]))
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }
}
