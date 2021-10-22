#### openfeign+sentinel工程
* 配置步骤:导入相关依赖，消费者端加上@EnableFeignClients注解，服务提供者端加上@EnableDiscoveryClient注解。
* 配置@FeignClient中value属性用于配置服务实例，与application.yml中的application-name相互对应，用于去配置中心拿对应的实例。
* 待接入有文件上传的feign调用











#### 填坑记录
* 由于版本不兼容，会造成运行时抛出spring循环依赖异常，（原因未查出,初步估计是加载mvc和feign的某个类时被循环依赖了）版本spring.cloud.alibaba.version为2.2.1.RELEASE,spring.cloud.version为Hoxton.SR11，springboot版本2.3.10.RELEASE
* 为每个feignclient单独配置configuration时，自定义配置类需要移除@configuration注解，并且feignclient需要配置contextid来作为唯一标识，实现不同的service有不同的配置。
