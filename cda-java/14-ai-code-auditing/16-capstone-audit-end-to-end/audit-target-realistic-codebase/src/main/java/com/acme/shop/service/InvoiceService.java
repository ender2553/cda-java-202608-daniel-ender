package com.acme.shop.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class InvoiceService {

    private static final String INVOICE_ROOT = "/var/app/invoices";

    public byte[] loadInvoice(String invoiceName) {
        try {
            Path path = Paths.get(INVOICE_ROOT, invoiceName);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("invoice not available: " + invoiceName, e);
        }
    }

    // The redirect target is a fixed internal path; callers cannot influence it.
    public String successRedirect() {
        String base = "/orders/confirmed";
        return base;
    }
}
