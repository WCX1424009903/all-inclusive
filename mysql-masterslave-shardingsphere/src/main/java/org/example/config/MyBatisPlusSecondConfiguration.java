package org.example.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.example.algorithm.HintShardingAlgorithmTables;
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
import java.util.*;
import java.util.stream.Stream;

/**
 * mybatis多数据源配置
 * @author WCX
 */
@Configuration
@MapperScan(basePackages = {MyBatisPlusSecondConfiguration.MAPPER_SECOND},
        sqlSessionTemplateRef = "seconddbSqlSessionTemplate")
public class MyBatisPlusSecondConfiguration {
    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    /**
     * mapper位置
     */
    public static final String MAPPER_SECOND = "org.example.mapper.second";
    /**
     * xml位置位置
     */
    public String[] mapperLocations = new String[]{
            "classpath*:/mapper/second/*.xml",
    };

    @Bean(name = "secondDataSource")
    @ConfigurationProperties("spring.datasource.second")
    public DataSource goodsdbDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "shardingSecondDataSource")
    public DataSource shardingSecondDataSource(@Qualifier("secondDataSource") DataSource secondDataSource) throws SQLException {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>(2);
        dataSourceMap.put("secondDataSource", secondDataSource);
        // sql日志打印
        Properties properties = new Properties();
        properties.setProperty("sql.show","true");
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, createShardingRuleConfiguration(),properties);
    }

    @Bean(name = "secondSqlSessionFactory")
    public SqlSessionFactory secondSqlSessionFactory(@Qualifier("shardingSecondDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 设置xml位置
        bean.setMapperLocations(resolveMapperLocations());
        return bean.getObject();
    }

    @Bean(name = "secondTransactionManager")
    public DataSourceTransactionManager secondTransactionManager(@Qualifier("shardingSecondDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "seconddbSqlSessionTemplate")
    public SqlSessionTemplate seconddbSqlSessionTemplate(@Qualifier("secondSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
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
        result.setDefaultDataSourceName("secondDataSource");
        result.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        return result;
    }
    /**
     *配置某表的分片规则
     */
    private static TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_order", "secondDataSource.t_order_${0..1}");
        // 配置分表规则--某个字段对应某个算法
        //result.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id","t_order_${order_id % 2}"));此种方式aop拦截不会生效
        result.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("orderNumber",new HintShardingAlgorithmTables()));
        result.setDatabaseShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        return result;
    }
}
