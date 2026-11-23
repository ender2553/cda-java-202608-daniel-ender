package com.rti.newservice.api;

import com.rti.integration.dto.ServiceReport;
import com.rti.newservice.client.MonolithClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a notification endpoint that pulls a report from the monolith.
 */
@RestController
public class NotificationController {

    private final MonolithClient client;

    public NotificationController(MonolithClient client) {
        this.client = client;
    }

    @GetMapping("/notify/{accountId}")
    public ServiceReport notify(@PathVariable String accountId) {
        return client.fetchReport(accountId);
    }
}
