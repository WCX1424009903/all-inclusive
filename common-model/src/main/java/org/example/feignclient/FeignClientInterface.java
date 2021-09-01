package org.example.feignclient;

/**
 * feignclien service配置常量接口
 * 用于feign服务发现，配置值为每个服务的application-name，在yml配置文件中即可查看
 * @author wcx
 * @date 2021/8/31 22:01
 */
public interface FeignClientInterface {
    /**
    * openfeign-sentinel服务
    */
   String OPENFEIGN_SENTINEL = "openfeign-sentinel";
    /**
     * nacos-config服务
     */
   String NACOS_CONFIG = "nacos-config";
}
