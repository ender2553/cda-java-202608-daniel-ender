package learn.secops.data;

import learn.secops.models.Host;
import learn.secops.models.HostCriticality;

import java.util.List;

public interface HostRepository {

    List<Host> findAll();

    List<Host> findByCriticality (HostCriticality criticality);

    Host findById(int hostId);

    Host add(Host host);

    boolean updateCriticality(int hostId, HostCriticality newCriticality);

    boolean deleteById(int hostId);

}
