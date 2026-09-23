package com.cybersoft.vehicle;

import com.cybersoft.vehicle.app.VehicleApplication;
import com.cybersoft.vehicle.crypto.FieldEncryptor;
import com.cybersoft.vehicle.crypto.PasswordHasher;
import com.cybersoft.vehicle.model.Car;
import com.cybersoft.vehicle.model.VehicleStatus;
import com.cybersoft.vehicle.repository.InMemoryVehicleRepository;
import com.cybersoft.vehicle.repository.VehicleRepository;
import com.cybersoft.vehicle.service.RatePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityTests {
    @Test
    void constructorRejectsBadVin() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Car(1, "BAD", "Honda", "Civic", 2024, VehicleStatus.AVAILABLE, List.of(), 5)
        );
    }

    @Test
    void copyInCopyOutProtectsMutableInput() {
        var notes = new ArrayList<>(List.of("safe"));
        var car = new Car(
                1,
                "1HGCM82633A004352",
                "Honda",
                "Civic",
                2024,
                VehicleStatus.AVAILABLE,
                notes,
                5
        );
        notes.add("mutated");
        assertEquals(List.of("safe"), car.getServiceNotes());
        assertThrows(UnsupportedOperationException.class, () -> car.getServiceNotes().add("x"));
    }

    @Test
    void hashVerifies() {
        var hasher = new PasswordHasher();
        char[] password = "CorrectHorse!42".toCharArray();
        String hash = hasher.hash(password);
        assertTrue(hasher.verify(password, hash));
        assertFalse(hasher.verify("WrongPassword!42".toCharArray(), hash));
    }

    @Test
    void aesGcmRejectsTampering() {
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        var encryptor = new FieldEncryptor(key);
        byte[] ciphertext = encryptor.encrypt("secret");
        ciphertext[ciphertext.length - 1] ^= 1;
        assertThrows(SecurityException.class, () -> encryptor.decrypt(ciphertext));
    }

    @Test
    void springContextProvidesDefaultDependencies() {
        try (var context = new AnnotationConfigApplicationContext(VehicleApplication.class)) {
            assertInstanceOf(InMemoryVehicleRepository.class, context.getBean(VehicleRepository.class));
            assertNotNull(context.getBean(RatePolicy.class));
            assertNotNull(context.getBean(PasswordHasher.class));
            assertNotNull(context.getBean(FieldEncryptor.class));
        }
    }
}
