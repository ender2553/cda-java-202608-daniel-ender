-- Day 3 demo schema: a minimal slice of the SecOps five-table schema.
-- Only host and ticket are needed to demonstrate the Repository pattern.
-- Run this against a local "secops" PostgreSQL database before class.

drop table if exists ticket;
drop table if exists host;

create table host (
    host_id      int generated always as identity primary key,
    hostname     varchar(100) not null,
    ip_address   varchar(45)  not null
);

create table ticket (
    ticket_id    int generated always as identity primary key,
    host_id      int not null references host (host_id),
    description  varchar(500) not null,
    status       varchar(20) not null
                 check (status in ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    opened_at    timestamp not null default now()
);
