gateway+sentinel网关工程
---

####sentinel控制台启动(1.8.1版本)
* 控制台文档介绍https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0
* 启动命令:java -Dserver.port=8002 -Dcsp.sentinel.dashboard.server=127.0.0.1:8002 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.1.jar
* 账号sentinel 密码 sentinel
---

* 网关作用：限流、权限控制、请求路由转发、熔断、负载均衡，不做任何业务处理。