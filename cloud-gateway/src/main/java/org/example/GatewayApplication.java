package org.example;


import org.example.config.CorsCustomiseMvcConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {CorsCustomiseMvcConfiguration.class})
@EnableDiscoveryClient
public class GatewayApplication
{
    public static void main(String[] args )
    {
        SpringApplication.run(GatewayApplication.class,args);
    }
}
