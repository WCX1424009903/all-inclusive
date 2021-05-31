package org.example;

import org.example.config.CorsCustomiseWebFluxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {CorsCustomiseWebFluxConfiguration.class})
@EnableDiscoveryClient
public class NacosServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosServerApplication.class,args);
    }
}
