#### common-starter-mongodb配置

* 常见用法：dao层通过继承MongoRepository接口类的方式，进行基本crud。或者通过MongoTemplate类进行相应的构造
* 集合id由MongoDB服务端生成，方便集群部署时生成分布式主键


#### 错误填坑记录
* MongoDB配置中密码需要加双引号，否则在操作MongoDB时会报错，即password: "123456"
