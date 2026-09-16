insert into host (hostname, ip_address) values
    ('web-01.internal', '10.0.1.11'),
    ('db-02.internal',  '10.0.1.22'),
    ('vpn-gw.internal', '10.0.1.33');

insert into service (host_id, name, port, status) values
    (1, 'nginx',    443, 'RUNNING'),
    (1, 'sshd',      22, 'RUNNING'),
    (2, 'postgres', 5432, 'RUNNING'),
    (3, 'openvpn',  1194, 'STOPPED');
