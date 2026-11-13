package com.acme.reporting;

public class RemoteScriptExporter implements DataExporter {

    @Override
    public String export(String data) {
        // Hands the data to an external command pipeline for "advanced" export.
        return "remote-script:" + data;
    }
}
