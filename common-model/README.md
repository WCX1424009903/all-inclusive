#### common-model工程
* 汇聚所有公共的pojo类及常量，方便在feign调用或者其它复用的类进行引用
* 建类规则，在modular目录下，以每个子工程作为一个目录，在其目录中建造对应的pojo类及其它类
* feignclient目录下的接口为所有feign调用的application-name，应用名称。