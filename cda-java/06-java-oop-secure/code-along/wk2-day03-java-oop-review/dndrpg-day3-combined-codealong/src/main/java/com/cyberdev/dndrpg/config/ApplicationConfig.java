package com.cyberdev.dndrpg.config;

import com.cyberdev.dndrpg.security.EncryptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration(proxyBeanMethods = false)
@ComponentScan("com.cyberdev.dndrpg")
public class ApplicationConfig {

    @Bean("emailEncryptionKey")
    public SecretKey emailEncryptionKey() {
        return EncryptionService.generateKey();
    }
}
