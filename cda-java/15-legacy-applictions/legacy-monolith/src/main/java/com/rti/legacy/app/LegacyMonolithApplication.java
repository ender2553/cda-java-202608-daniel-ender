package com.rti.legacy.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.rti.legacy")
public class LegacyMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyMonolithApplication.class, args);
    }
}
