package org.example;

import org.example.config.CorsCustomiseWebFluxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {CorsCustomiseWebFluxConfiguration.class})
public class ShardingshphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShardingshphereApplication.class,args);
    }
}
