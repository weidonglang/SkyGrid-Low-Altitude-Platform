package com.lowaltitude.conflict;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@EnableDiscoveryClient
@EnableFeignClients
@EnableRabbit
@SpringBootApplication(scanBasePackages = "com.lowaltitude")
public class ConflictNotifyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConflictNotifyServiceApplication.class, args);
    }
}
