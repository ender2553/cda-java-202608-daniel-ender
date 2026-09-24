create table if not exists paint_user (
    id bigserial primary key,
    username varchar(32) not null unique,
    password_hash varchar(100) not null,
    created_at timestamptz not null default current_timestamp
);
create table if not exists paint_estimate (
    id bigserial primary key,
    user_id bigint not null references paint_user(id) on delete cascade,
    room_length numeric(8,2) not null,
    room_width numeric(8,2) not null,
    room_height numeric(8,2) not null,
    coats integer not null,
    color varchar(30) not null,
    gallons numeric(8,2) not null,
    paint_cost numeric(12,2) not null,
    created_at timestamptz not null default current_timestamp
);
