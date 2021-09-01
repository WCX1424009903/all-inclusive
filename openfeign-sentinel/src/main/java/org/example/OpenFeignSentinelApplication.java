package org.example;

import org.example.config.CorsCustomiseWebFluxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = CorsCustomiseWebFluxConfiguration.class)
@EnableFeignClients
@EnableDiscoveryClient
public class OpenFeignSentinelApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenFeignSentinelApplication.class,args);
    }
}
