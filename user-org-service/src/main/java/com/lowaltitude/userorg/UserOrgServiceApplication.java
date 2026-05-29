package com.lowaltitude.userorg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.lowaltitude.userorg.mapper")
@SpringBootApplication(scanBasePackages = "com.lowaltitude")
public class UserOrgServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserOrgServiceApplication.class, args);
    }
}
