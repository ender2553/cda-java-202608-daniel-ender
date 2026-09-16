insert into host (hostname, ip_address) values
    ('web-01.internal', '10.0.1.11'),
    ('db-02.internal',  '10.0.1.22'),
    ('vpn-gw.internal', '10.0.1.33');

insert into ticket (host_id, description, status) values
    (1, 'Unpatched TLS library flagged by scanner', 'OPEN'),
    (1, 'Unusual outbound traffic on port 4444',     'IN_PROGRESS'),
    (2, 'Failed login threshold exceeded (12 attempts)', 'OPEN'),
    (3, 'VPN certificate expires in 7 days',         'RESOLVED');
