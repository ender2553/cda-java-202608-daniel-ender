package learn.secops.models;

import java.util.Objects;

public class Host {
    private final int hostId;
    private final String hostname;
    private final String ipAdress;

    public Host(int hostId, String hostname, String ipAdress){
        if (hostId <= 0){
            throw new IllegalArgumentException("hostId must be positive");
        }

        if (hostname == null || hostname.isBlank()){
            throw new IllegalArgumentException("hostname is required");
        }
        if (ipAdress == null || ipAdress.isBlank()){
            throw new IllegalArgumentException("ipAdress is required");
        }

        this.hostId = hostId;
        this.hostname = hostname;
        this.ipAdress = ipAdress;
    }

    public int getHostId() {
        return hostId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return hostId == host.hostId && Objects.equals(hostname, host.hostname) && Objects.equals(ipAdress, host.ipAdress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostId, hostname, ipAdress);
    }
}
