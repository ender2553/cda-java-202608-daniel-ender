create table if not exists rpg_user (
    id bigserial primary key, username varchar(32) not null unique,
    password_hash varchar(100) not null, created_at timestamptz not null default current_timestamp
);
create table if not exists character_save (
    id bigserial primary key, user_id bigint not null unique references rpg_user(id) on delete cascade,
    character_name varchar(40) not null, level integer not null, experience integer not null,
    health integer not null, max_health integer not null, gold integer not null,
    location varchar(60) not null, equipped_weapon varchar(40), equipped_armor varchar(40),
    inventory_json text not null, updated_at timestamptz not null default current_timestamp
);
