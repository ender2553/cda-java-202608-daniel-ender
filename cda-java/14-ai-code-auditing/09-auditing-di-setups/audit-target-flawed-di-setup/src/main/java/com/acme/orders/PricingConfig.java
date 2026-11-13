package com.acme.orders;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class PricingConfig {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST)
    public PricingStrategy pricingStrategy(HttpServletRequest request) throws Exception {
        String strategyClass = request.getHeader("X-Pricing-Strategy");
        if (strategyClass == null || strategyClass.isBlank()) {
            return new StandardPricingStrategy();
        }
        return (PricingStrategy) Class.forName(strategyClass)
                .getDeclaredConstructor()
                .newInstance();
    }
}
