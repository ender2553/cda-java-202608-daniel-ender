package learn.secops.data;

import learn.secops.models.Service;

import java.util.List;

/**
 * The Repository contract you are implementing this afternoon.
 * Do not change this interface.
 */
public interface ServiceRepository {

    List<Service> findAll();

    List<Service> findByHostId(int hostId);

    Service findById(int serviceId);

    Service add(Service service);

    boolean update(Service service);

    boolean deleteById(int serviceId);
}
