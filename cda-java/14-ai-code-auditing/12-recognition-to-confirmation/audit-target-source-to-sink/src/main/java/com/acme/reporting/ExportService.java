package com.acme.reporting;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ExportService {

    private static final String EXPORT_DIR = "/var/acme/exports/";

    public byte[] readExport(String name) throws Exception {
        Path path = Paths.get(EXPORT_DIR + name);
        return Files.readAllBytes(path);
    }
}
