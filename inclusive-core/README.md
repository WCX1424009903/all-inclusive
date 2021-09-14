### inclusive工程公共配置类文件

---
* 采用spring.factories进行加载配置文件，相当于spi机制,拔插即用
* 不需要的配置在springBootApplication注解中exclude中进行排除
* pom文件中引入spring-boot-maven-plugin时，如果进行install会报错
---

### 配置类介绍
* JackJsonSerilaizeConfig前端序列化配置
* GlobalExceptionHandler全局异常处理配置
* CorsCustomiseMvcConfiguration跨域配置springmvc
* CorsCustomiseWebFluxConfiguration跨域配置springwebflux

