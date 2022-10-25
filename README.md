# all-inclusive项目注意事项
* springboot、springcloud、springcloud alibaba版本兼容参照网址:https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E
* 注：所有的版本都采用parent pom进行管理，在dependencyManagement中申明子工程需要的引用。
* parent依赖版本不能在property属性中进行声明，必须显示声明，否则报错。
* 对于公共模块依赖的代码，最好使用maven-source-plugin插件将源码打包，debug时方便调试。
* common-model工程汇聚所有公共的pojo类及常量，方便在feign调用或者其它复用的类进行引用

___
maven版本: apache-maven-3.5.3

---
nacos-org.example.config-yml文件夹存放nacos导出配置文件（nacos版本2.0.0）
___


#错误填坑记录
* 对于debug时报错command line too long（依赖的工程太多，路径太长），由于操作系统对运行命令行的限制关系导致，解决办法在idea配置Shorten command line中采用classpath file进行压缩。
* 出现“错误: 找不到或无法加载主类”，将所在工程进行clean install（原因：项目java代码和target不一致造成，未找到根本解决办法）。
* 如果新建子工程导包失败，或者某些yml文件不高亮，可能是idea中进行了pom文件忽略，进入setting找到maven ignored files进行配置。
* 对于打包出来的jar，没有依赖其它jar包，采用springboot-maven-plugin中repackage的方式进行打包。
* maven中重复引入问题，可以考虑使用scope标签指定范围system、runtime、provide、test、compile。
* 对于项目中**.xml文件放到src/main/java中，如多数据源配置xml放在package中，在打包成jar包时没有xml的问题，需要在pom文件中设置build.resource.directory内容，详情见mysql-masterslave-shardingsphere模块pom文件。
* maven打包带源码，需要在pom文件中配置maven-source-plugin插件。

