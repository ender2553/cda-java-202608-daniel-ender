package learn.secops.data;

import learn.secops.models.Host;

import java.util.List;

// Domain-facing contract for retrieving hosts
public interface HostRepository {

    List<Host> findAll();

    Host findById (int hostId);
}
