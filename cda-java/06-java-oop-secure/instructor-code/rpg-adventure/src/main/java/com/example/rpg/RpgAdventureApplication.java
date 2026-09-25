package com.example.rpg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RpgAdventureApplication {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext ignored =
                     SpringApplication.run(RpgAdventureApplication.class, args)) {
            // The console runner owns the game loop; returning from it means the player has finished.
        }
    }
}
