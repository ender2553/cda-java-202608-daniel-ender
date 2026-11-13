package com.acme.reporting;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

@Service
public class IngestService {

    public String restore(String payload) throws Exception {
        byte[] raw = Base64.getDecoder().decode(payload);
        try (ObjectInputStream ois =
                     new ObjectInputStream(new ByteArrayInputStream(raw))) {
            Object record = ois.readObject();
            return "ingested: " + record.getClass().getName();
        }
    }
}
