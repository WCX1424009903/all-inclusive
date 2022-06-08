package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableFeignClients
@EnableDiscoveryClient
public class OpenFeignSentinelApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenFeignSentinelApplication.class,args);
    }
}
