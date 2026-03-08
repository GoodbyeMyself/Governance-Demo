package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DataMetadataApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataMetadataApplication.class, args);
    }
}
