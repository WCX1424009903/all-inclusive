package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;

@SpringCloudApplication
@EnableConfigurationProperties
public class ShardingshphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShardingshphereApplication.class,args);
    }
}
