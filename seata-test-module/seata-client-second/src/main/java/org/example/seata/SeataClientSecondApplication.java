package org.example.seata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.example.**.mapper")
@EnableFeignClients
public class SeataClientSecondApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SeataClientSecondApplication.class,args);
    }
}
