package com.cybersoft.vehicle.app;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.cybersoft.vehicle")
public final class VehicleApplication {
    private VehicleApplication() {
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(VehicleApplication.class)) {
            context.getBean(VehicleDemo.class).run();
        }
    }
}
