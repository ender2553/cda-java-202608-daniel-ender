drop table if exists service;
drop table if exists host;

create table host (
    host_id      int generated always as identity primary key,
    hostname     varchar(100) not null,
    ip_address   varchar(45)  not null
);

create table service (
    service_id   int generated always as identity primary key,
    host_id      int not null references host (host_id),
    name         varchar(100) not null,
    port         int not null,
    status       varchar(20) not null
                 check (status in ('RUNNING', 'STOPPED', 'UNKNOWN'))
);
