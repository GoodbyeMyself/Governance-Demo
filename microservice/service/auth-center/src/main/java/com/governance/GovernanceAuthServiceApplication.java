package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GovernanceAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovernanceAuthServiceApplication.class, args);
    }
}
