package com.cybersoft.vehicle.app;

import com.cybersoft.vehicle.crypto.FieldEncryptor;
import com.cybersoft.vehicle.crypto.PasswordHasher;
import com.cybersoft.vehicle.model.Car;
import com.cybersoft.vehicle.model.CustomerProfile;
import com.cybersoft.vehicle.model.Truck;
import com.cybersoft.vehicle.model.Vehicle;
import com.cybersoft.vehicle.model.VehicleStatus;
import com.cybersoft.vehicle.model.VehicleSummary;
import com.cybersoft.vehicle.repository.VehicleRepository;
import com.cybersoft.vehicle.service.RatePolicy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public final class VehicleDemo {
    private final RatePolicy ratePolicy;
    private final VehicleRepository vehicleRepository;
    private final PasswordHasher passwordHasher;
    private final FieldEncryptor fieldEncryptor;

    public VehicleDemo(
            RatePolicy ratePolicy,
            VehicleRepository vehicleRepository,
            PasswordHasher passwordHasher,
            FieldEncryptor fieldEncryptor
    ) {
        this.ratePolicy = ratePolicy;
        this.vehicleRepository = vehicleRepository;
        this.passwordHasher = passwordHasher;
        this.fieldEncryptor = fieldEncryptor;
    }

    public void run() {
        Vehicle car = new Car(
                1,
                "1HGCM82633A004352",
                "Honda",
                "Accord",
                2024,
                VehicleStatus.AVAILABLE,
                List.of("Initial inspection"),
                5
        );
        Vehicle truck = new Truck(
                2,
                "1FTFW1E50JFA00001",
                "Ford",
                "F-150",
                2023,
                VehicleStatus.AVAILABLE,
                List.of(),
                1800
        );

        for (Vehicle vehicle : List.of(car, truck)) {
            System.out.printf("%-28s daily=$%.2f%n", vehicle.description(), vehicle.dailyRate());
        }

        System.out.printf("3-day car quote: $%.2f%n", ratePolicy.quote(car, 3));
        vehicleRepository.save(new VehicleSummary(
                car.getId(),
                car.getVin(),
                car.description(),
                car.getStatus()
        ));
        System.out.println("Repository: " + vehicleRepository.findAll());

        char[] password = "CorrectHorse!42".toCharArray();
        try {
            String stored = passwordHasher.hash(password);
            System.out.println("Password verified: " + passwordHasher.verify(password, stored));
        } finally {
            Arrays.fill(password, '\0');
        }

        byte[] encrypted = fieldEncryptor.encrypt("D123-4567-8901");
        CustomerProfile customer = new CustomerProfile(1, "Alex Driver", encrypted);
        System.out.println("Encrypted license bytes: " + customer.encryptedLicense().length);
        System.out.println("Decrypted for authorized use: " + fieldEncryptor.decrypt(customer.encryptedLicense()));
    }
}
