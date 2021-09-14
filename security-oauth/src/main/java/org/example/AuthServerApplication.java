package org.example;

import org.example.config.CorsCustomiseWebFluxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * oauth认证服务
 * @author wcx
 * @date 2021/9/12
 */
@SpringBootApplication(exclude = CorsCustomiseWebFluxConfiguration.class)
public class AuthServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class,args);
    }
}
