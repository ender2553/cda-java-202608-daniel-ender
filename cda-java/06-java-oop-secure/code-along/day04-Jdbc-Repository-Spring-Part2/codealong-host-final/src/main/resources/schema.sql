drop table if exists host;

create table host (
    host_id      int generated always as identity primary key,
    hostname     varchar(100) not null,
    ip_address   varchar(45)  not null,
    criticality  varchar(20)  not null
                 check (criticality in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);
