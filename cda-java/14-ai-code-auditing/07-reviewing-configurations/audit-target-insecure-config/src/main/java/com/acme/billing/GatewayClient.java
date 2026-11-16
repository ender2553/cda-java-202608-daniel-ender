package com.acme.billing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GatewayClient {

    private final String baseUrl;
    private final String apiKey;
    private final boolean verifyTls;

    public GatewayClient(
            @Value("${billing.external-gateway.base-url}") String baseUrl,
            @Value("${billing.external-gateway.api-key}") String apiKey,
            @Value("${billing.external-gateway.verify-tls}") boolean verifyTls) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.verifyTls = verifyTls;
    }

    public String describe() {
        return "gateway=" + baseUrl + " verifyTls=" + verifyTls;
    }

    public String authHeader() {
        return "Bearer " + apiKey;
    }
}
