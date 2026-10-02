package demo.clinic.service;

import demo.clinic.domain.Appointment;
import demo.clinic.log.CheckInCounter;
import demo.clinic.repository.AppointmentRepository;
import demo.clinic.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Checks a patient in for their next booked appointment. */
@Service
public class CheckInService {

    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final CheckInCounter counter;

    public CheckInService(PatientRepository patients, AppointmentRepository appointments, CheckInCounter counter) {
        this.patients = patients;
        this.appointments = appointments;
        this.counter = counter;
    }

    @Transactional
    public Appointment checkIn(String patientId) {
        patients.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("No patient " + patientId));
        Appointment next = appointments.nextBookedFor(patientId)
                .orElseThrow(() -> new IllegalArgumentException("No booked appointment for " + patientId));
        appointments.markCheckedIn(next.id());
        counter.recordCheckIn();
        return next;
    }
}
