# all-inclusive
主从和分库分表 模块

##masterslave 和ShardingSphere-jdbc
* 多数据源配置原理：orm框架路由时，会根据配置的datasource规则，路由至对应的执行数据源。sharing-jdbc本质也是配置datasource进行分库分表。
* mysql脚本位置resource/sql下面
* 待接入seata分布式事务。
* 待接入数据脱敏处理。



####docker启动mysql命令
docker run  -p 3306:3306 --name mysql-server -v /home/docker/conf:/etc/mysql -v /home/docker/logs:/var/log/mysql -v /home/docker/data:/var/lib/mysql -v /home/docker/files:/var/lib/mysql-files/ -e MYSQL_ROOT_PASSWORD=123456 -d mysql
###填坑记录
* @ConfigurationProperties映射属性需加上@EnableConfigurationProperties，应用于datasource配置，映射时由方法还是属性进行待确定。
* PaginationInnerInterceptor配置基本分页，额外的分页参数配置继承Page对象，未配置pagehelper(可动态方言配置)第三方包进行分页。ShardingAlgorithm
* 数据库分片不会将主键作为分片字段，用某个业务字段且值为唯一进行分片，b+tree在构建索引时主键无规律会进行重排序。
* sharding-sphere5.0.0版本没有找到自定义分片策略，采用spi机制仍然没生效，无法aop进行强制路由，采用4.1.1版本。
* sharding4.1.1版本，采用java代码配置，引入sharding-jdbc-spring-boot-starter依赖会报错，找不到数据源，引入jdbc-core依赖。
* sharding5.0版本不会接管所有数据源datasource配置，sharding4.1版本，如果数据源不配置分库分表策略，则会报错，某个mapper找不到。
* sharding4.1.1版本中table分片时采用StandardShardingStrategyConfiguration配置（将分片字段传入进来），HintShardingStrategyConfiguration配置会导致aop强制路由失效。
* 


#### sharding-jdbc4.1.1配置流程 
* 文档网址 https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/
1. 配置分表数据源datasource：
   - 首先配置普通数据源，通过@ConfigurationProperties将yml文件中属性映射到druiddatasource中，属性相对应，并加入到ioc容器中。
   - 第二步配置sharding-jdbc的真实数据源，通过ShardingDataSourceFactory工厂类的createDataSource方法将普通数据源、分库分表规则、属性规则加入进来。
   - 第三步sharing-jdbc分库分表规则配置：ShardingRuleConfiguration类用于规则配置进行中间协调。ShardingStrategyConfiguration类用于分片策略配置，将分片键和分片算法结合起来。ShardingAlgorithm类用于自定义分片算法，根据不同的子类选择不同的分片算法，通过重写doSharding方法进行，如对某个数取余得到数据表的后缀。
   - 最后将sharding-jdbc的数据源加入到SqlSessionFactory、DataSourceTransactionManager、SqlSessionTemplate中，与mybatis相互结合起来。
2. aop强制路由：
   - 自定义ShardingAop注解
   - 通过ShardingAopUtil拦截类进行拦截注解，并获取其中的值，再通过官方提供的HintManager类中的addTableShardingValue()方法将分片值加入到强制路由类中。
   - 其中ShardingAop注解中的值采用spel表达式，AspectSupportUtils类用于计算spel表达式的值。
3. spel表达式规则：
   - spel表达式规则，必须以#开头进行计算，例如: #a0.xx  a表示某个方法对象，0表示方法参数对应的下标，xx代表对象的属性。
   - 有如下方法：pulic void 某某方法名(Object object,String string,Integer[] integers)。
   - 1.获取object对应的某个属性，则表达式为@ShardingAop(value = "#a0.属性名")。
   - 2.获取string值，则表达式为@ShardingAop(value = "#a1")。
   - 3.获取Integer[]某个值，则表达式为@ShardingAop(value = "#a2[下标]")。
   - 上述0,1,2对应的方法参数的下标，本质是反射时获取方法时返回method[]数组。