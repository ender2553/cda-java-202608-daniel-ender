package learn.secops.data;

import learn.secops.models.SecurityEvent;

import java.util.List;

public interface SecurityEventRepository {

    List<SecurityEvent> findAll();

    List<SecurityEvent> findByHostId(int hostId);

    List<SecurityEvent> findUnacknowledged();

    SecurityEvent findById(int eventId);

    SecurityEvent add(SecurityEvent event);

    boolean acknowledge(int eventId);

    boolean deleteById(int eventId);
}

