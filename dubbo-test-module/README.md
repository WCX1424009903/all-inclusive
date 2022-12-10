### dubbo-test-module工程

### [dubbo官网](https://dubbo.apache.org/zh/index.html)

- - -

### 工程说明

* 目前dubbo版本为2.7.8，采用nacos为注册中心。由于采用3.0版本，要求nacos-client版本为2.0以上，否则会报错。
* [dubbo-producer](dubbo-producer) 为服务提供者，通过@DubboService注解实现服务提供。其主要实现[dubbo-api](dubbo-api)工程中抽离的公共接口。
* [dubbo-consumer](dubbo-consumer) 为服务调用者，通过@DubboReference注解进行服务调用。
* 整合nacos作为注册中心时，可以将dubbo的服务发现单独用命名空间隔离。
* dubbo的负载均衡策略主要通过AbstractLoadBalance抽象类的子类进行实现。有随机、轮询、一致性 Hash等算法。负载均衡策略采用SPI的方式进行加载，在org.apache.dubbo.rpc.cluster.LoadBalance文件中即可查看。

