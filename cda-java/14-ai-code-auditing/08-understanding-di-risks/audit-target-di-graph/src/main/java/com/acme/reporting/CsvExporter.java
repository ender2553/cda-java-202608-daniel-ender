package com.acme.reporting;

public class CsvExporter implements DataExporter {

    @Override
    public String export(String data) {
        return "csv:" + data;
    }
}
