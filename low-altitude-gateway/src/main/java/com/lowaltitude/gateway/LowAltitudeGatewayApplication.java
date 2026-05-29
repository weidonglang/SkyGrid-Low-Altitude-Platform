package com.lowaltitude.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.lowaltitude")
public class LowAltitudeGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LowAltitudeGatewayApplication.class, args);
    }
}
