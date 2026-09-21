package learn.secops.models;

import java.util.Objects;
import java.util.regex.Pattern;

public class Host {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}$"
    );

    private final int hostId;
    private final String hostname;
    private final String ipAddress;
    private final HostCriticality criticality;

    /** Creates a barnd-new host that has not been saved yet */
    public Host(String hostname, String ipAddress, HostCriticality criticality){
//        this.hostId = 0;
//        this.hostname = hostname;
//        this.ipAddress = ipAddress;
//        this.criticality = criticality;

        this(0, hostname, ipAddress, criticality);

    }

    /** Reconstructs a host that already exists in the database */
    public Host (int hostId, String hostname, String ipAddress, HostCriticality criticality) {
        if (hostId < 0 ){
            throw new IllegalArgumentException("hostId cannot be negative");
        }
        if (hostname == null || hostname.isBlank()){
            throw new IllegalArgumentException("hostname is required");
        }
        if (hostname.length() > 100) {
            throw new IllegalArgumentException("hostname must be 100 characters of less");
        }
        if (ipAddress == null || !IPV4_PATTERN.matcher(ipAddress).matches()){
            throw new IllegalArgumentException("ipAddress must be a valid IPv4 address");
        }

        this.hostId = hostId;
        this.hostname = hostname;
        this.ipAddress = ipAddress.trim();
        this.criticality = criticality == null ? HostCriticality.HIGH : criticality;  //Q:
    }

    public int getHostId() {
        return hostId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public HostCriticality getCriticality() {
        return criticality;
    }

    @Override
    public String toString() {
        return "Host{" +
                "hostId=" + hostId +
                ", hostname='" + hostname + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", criticality=" + criticality +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return hostId == host.hostId && Objects.equals(hostname, host.hostname) && Objects.equals(ipAddress, host.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostId, hostname, ipAddress);
    }
}
