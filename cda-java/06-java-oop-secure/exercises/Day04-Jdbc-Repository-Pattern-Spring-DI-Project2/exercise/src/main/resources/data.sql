insert into host (hostname, ip_address) values
    ('web-01.internal', '10.0.1.11'),
    ('db-02.internal',  '10.0.1.22'),
    ('vpn-gw.internal', '10.0.1.33');

insert into event (host_id, event_type, severity, description, detected_at, acknowledged, acknowledged_at) values
    (1, 'PORT_SCAN',           'MEDIUM',   'Sequential port scan detected from external range', '2026-09-01 03:14:00', false, null),
    (1, 'LOGIN_FAILURE',       'LOW',      'Three failed SSH login attempts',                    '2026-09-02 08:02:00', true,  '2026-09-02 08:10:00'),
    (2, 'MALWARE_DETECTED',    'CRITICAL', 'Signature match for known ransomware dropper',       '2026-09-03 22:47:00', false, null),
    (3, 'CONFIG_CHANGE',       'HIGH',     'VPN split-tunneling enabled outside change window',  '2026-09-04 14:20:00', false, null);
