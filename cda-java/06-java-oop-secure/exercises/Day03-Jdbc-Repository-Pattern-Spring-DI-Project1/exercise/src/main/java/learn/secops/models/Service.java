package learn.secops.models;

import java.util.Objects;

/**
 * A network service running on a monitored host (e.g. "sshd" on
 * host 2, port 22). This class is complete -- you will not edit it.
 * It follows the same encapsulation habits as Ticket from the Day 3
 * lecture: private fields, a validating constructor, no raw setters
 * for hostId or serviceId.
 */
public class Service {

    private final int serviceId;
    private final int hostId;
    private String name;
    private int port;
    private ServiceStatus status;

    /** Creates a brand-new service that has not been saved yet. */
    public Service(int hostId, String name, int port, ServiceStatus status) {
        this(0, hostId, name, port, status);
    }

    /** Reconstructs a service that already exists in the database. */
    public Service(int serviceId, int hostId, String name, int port, ServiceStatus status) {
        if (hostId <= 0) {
            throw new IllegalArgumentException("hostId must be a positive, existing host id");
        }
        this.serviceId = serviceId;
        this.hostId = hostId;
        setName(name);
        setPort(port);
        this.status = status == null ? ServiceStatus.UNKNOWN : status;
    }

    public int getServiceId() {
        return serviceId;
    }

    public int getHostId() {
        return hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("name must be 100 characters or fewer");
        }
        this.name = name.trim();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        this.port = port;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status == null ? ServiceStatus.UNKNOWN : status;
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceId=" + serviceId +
                ", hostId=" + hostId +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return serviceId == service.serviceId &&
                hostId == service.hostId &&
                port == service.port &&
                Objects.equals(name, service.name) &&
                status == service.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, hostId, name, port, status);
    }
}
