package com.acme.reporting;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InternalOnlyInterceptor internalOnlyInterceptor;

    public WebConfig(InternalOnlyInterceptor internalOnlyInterceptor) {
        this.internalOnlyInterceptor = internalOnlyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalOnlyInterceptor)
                .addPathPatterns("/reports/ingest");
    }
}
