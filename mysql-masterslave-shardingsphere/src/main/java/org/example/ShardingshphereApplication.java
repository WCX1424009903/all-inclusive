package org.example;

import org.example.config.CorsCustomiseWebFluxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {CorsCustomiseWebFluxConfiguration.class})
@EnableConfigurationProperties
public class ShardingshphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShardingshphereApplication.class,args);
    }
}
