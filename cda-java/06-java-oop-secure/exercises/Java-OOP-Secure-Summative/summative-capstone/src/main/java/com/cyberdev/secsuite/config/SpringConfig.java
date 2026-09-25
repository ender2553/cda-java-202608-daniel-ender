package com.cyberdev.secsuite.config;

import com.cyberdev.secsuite.Main;
import com.cyberdev.secsuite.ui.ConsoleUI;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

@SpringBootApplication(scanBasePackages = "com.cyberdev.secsuite")
public class SpringConfig {
    @Bean public Clock clock() { return Main.resolveClock(); }
    @Bean public SecretKey encryptionKey() { return EncryptionKeyConfig.loadOrGenerateKey(); }
    @Bean public ConsoleUI consoleUI() {
        return new ConsoleUI(System.console(), new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), System.out);
    }
}
