package com.acme.shop.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class InvoiceService {

    private static final Path INVOICE_ROOT =
            Paths.get("/var/app/invoices")
                    .toAbsolutePath()
                    .normalize();

    public byte[] loadInvoice(String invoiceName) {

        if (invoiceName == null ||
                !invoiceName.matches("[A-Za-z0-9_-]+\\.pdf")) {
            throw new IllegalArgumentException("Invalid invoice name");
        }

        try {
            Path realRoot = INVOICE_ROOT.toRealPath();

            Path candidate = realRoot.resolve(invoiceName).normalize();

            if (!candidate.startsWith(realRoot)) {
                throw new IllegalArgumentException("Invalid invoice path");
            }

            Path realFile = candidate.toRealPath();

            if (!realFile.startsWith(realRoot)
                    || !Files.isRegularFile(realFile)
                    || !Files.isReadable(realFile)) {
                throw new IllegalArgumentException("Invoice unavailable");
            }

            return Files.readAllBytes(realFile);

        } catch (IOException e) {
            throw new IllegalArgumentException("Invoice unavailable");
        }
    }
}