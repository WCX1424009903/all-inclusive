package org.example;


import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

@SpringCloudApplication
public class DebugApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(DebugApplication.class,args);
    }
}
