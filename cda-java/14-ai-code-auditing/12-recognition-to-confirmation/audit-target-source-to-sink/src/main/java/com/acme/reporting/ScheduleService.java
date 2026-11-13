package com.acme.reporting;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    // Each accepted period maps to a fixed, hard-coded script name.
    private static final Map<String, String> ROLLUP_SCRIPTS = Map.of(
            "daily", "rollup_daily.sh",
            "weekly", "rollup_weekly.sh",
            "monthly", "rollup_monthly.sh");

    private static final String SCRIPT_DIR = "/opt/acme/bin/";

    public String runRollup(String period) throws Exception {
        String script = ROLLUP_SCRIPTS.get(period);
        if (script == null) {
            return "unknown period";
        }
        String[] cmd = {"/bin/sh", "-c", SCRIPT_DIR + script};
        Process process = Runtime.getRuntime().exec(cmd);
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
