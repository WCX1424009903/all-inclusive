### seata(1.5.2)客户端工程模块

* seata-server在nacos上的命名空间为18d864b2-80c2-413a-a6d6-26c03c969f0e
1. 新建seata_client_first数据库，并执行seata_client_first.sql脚本。
2. 新建seata_client_second数据库，并执行seata_client_second.sql脚本。
3. 启动两个服务，就可以在执行分布式数据回滚。


### 错误填坑记录

* 对于引入seata-spring-boot-starter包时，微服务feign需要配置xid传递，否则被调用方不加事务回滚注解时，无法进行回滚。如果不想自己配置xid传递，可引入spring-cloud-starter-alibaba-seata包，需要受限于alibaba当前版本。
* 对于配置了全局异常处理时，异常可能被ExceptionHandler所捕获，从而造成事务回滚不了，需要在调用方发起检查。