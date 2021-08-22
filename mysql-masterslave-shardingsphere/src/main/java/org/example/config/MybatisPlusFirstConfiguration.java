package org.example.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
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

    @Bean(name = "shardingSecondDataSourceFirst")
    public DataSource shardingSecondDataSource(@Qualifier("firstDataSource") DataSource firstDataSource) throws SQLException {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>(2);
        dataSourceMap.put("firstDataSource", firstDataSource);
        // sql日志打印
        Properties properties = new Properties();
        properties.setProperty("sql.show","true");
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, createShardingRuleConfiguration(),properties);
    }

    @Bean(name = "firstSqlSessionFactory")
    public SqlSessionFactory firstSqlSessionFactory(@Qualifier("shardingSecondDataSourceFirst") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置xml位置
        bean.setMapperLocations(resolveMapperLocations());
        return bean.getObject();
    }

    @Bean(name = "firstDataSourceTransactionManager")
    public DataSourceTransactionManager firstDataSourceTransactionManager(@Qualifier("shardingSecondDataSourceFirst") DataSource dataSource) {
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

    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.setDefaultDataSourceName("firstDataSource");
        result.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        return result;
    }
    /**
     *配置某表的分片规则
     */
    private static TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("user");
        // 配置分表规则--某个字段对应某个算法
        result.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        result.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        return result;
    }

}
