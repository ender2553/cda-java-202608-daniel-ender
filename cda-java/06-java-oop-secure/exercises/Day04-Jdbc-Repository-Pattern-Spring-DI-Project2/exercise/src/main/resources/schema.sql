drop table if exists event;
drop table if exists host;

create table host (
    host_id      int generated always as identity primary key,
    hostname     varchar(100) not null,
    ip_address   varchar(45)  not null
);

create table event (
    event_id       int generated always as identity primary key,
    host_id        int not null references host (host_id),
    event_type     varchar(30) not null
                   check (event_type in ('LOGIN_FAILURE', 'MALWARE_DETECTED',
                          'PORT_SCAN', 'CONFIG_CHANGE', 'UNAUTHORIZED_ACCESS')),
    severity       varchar(20) not null
                   check (severity in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description    varchar(500) not null,
    detected_at    timestamp not null,
    acknowledged   boolean not null default false,
    acknowledged_at timestamp null
);
