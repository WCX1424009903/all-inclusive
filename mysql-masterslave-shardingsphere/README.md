# all-inclusive
主从和分库分表 模块

##masterslave 和ShardingSphere-jdbc
* 多数据源配置原理：orm框架路由时，根据配置的datasource规则，找到对应的执行数据源。sharing-jdbc也是配置datasource。
* sharding-sphere数据源配置步骤：（1）.配置那些需要分库的db,并且设置分片算法。 （2）.配置那些需要分库的table，并设置分片算法。 （3）最后在添加到datasource中。
* 



####docker启动mysql命令
docker run  -p 3306:3306 --name mysql-server -v /home/docker/conf:/etc/mysql -v /home/docker/logs:/var/log/mysql -v /home/docker/data:/var/lib/mysql -v /home/docker/files:/var/lib/mysql-files/ -e MYSQL_ROOT_PASSWORD=123456 -d mysql
###填坑记录
* @ConfigurationProperties映射属性需加上@EnableConfigurationProperties，应用于datasource配置，映射时由方法还是属性进行待确定
* PaginationInnerInterceptor配置基本分页，额外的分页参数配置继承Page对象，未配置pagehelper(可动态方言配置)第三方包进行分页