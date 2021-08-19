package org.example.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
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
@MapperScan(basePackages = MyBatisPlusSecondConfiguration.MAPPER_SECOND,sqlSessionTemplateRef = "seconddbSqlSessionTemplate")
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
            "classpath*:/mapper/second/*.xml"
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
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(createShardingRuleConfiguration()),properties);
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
        result.getTables().add(getOrderTableRuleConfiguration());
        // 配置分表算法
        Properties tableShardingAlgorithmrProps = new Properties();
        tableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        // 与某表的分片规则算法对应
        result.getShardingAlgorithms().put("tableShardingAlgorithm",new ShardingSphereAlgorithmConfiguration("INLINE",tableShardingAlgorithmrProps));
        return result;
    }
    /**
     *配置某表的分片规则
     */
    private static ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "secondDataSource.t_order_${0..1}");
        // 配置分表规则--某个字段对应某个算法
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id","tableShardingAlgorithm"));
        result.setDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
}
