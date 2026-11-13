package com.acme.portal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifierConfig {

    @Bean
    public Notifier notifier(@Value("${portal.notifier.class}") String notifierClass)
            throws Exception {
        return (Notifier) Class.forName(notifierClass)
                .getDeclaredConstructor()
                .newInstance();
    }
}
