package com.rti.newservice.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.rti.newservice")
public class NewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewServiceApplication.class, args);
    }
}
