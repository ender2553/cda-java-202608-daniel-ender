-- Dungeon Crawler Arena -- PostgreSQL schema
--
-- Setup:
--   createdb dndrpg
--   psql dndrpg -f schema/schema.sql
--
-- Then point the app at it:
--   export DNDRPG_DB_URL=jdbc:postgresql://localhost:5432/dndrpg
--   export DNDRPG_DB_USER=<your user>
--   export DNDRPG_DB_PASSWORD=<your password>

CREATE TABLE IF NOT EXISTS game_class (
    character_class VARCHAR(20) PRIMARY KEY,
    base_hp         INTEGER NOT NULL CHECK (base_hp > 0),
    base_attack     INTEGER NOT NULL CHECK (base_attack >= 0),
    base_defense    INTEGER NOT NULL CHECK (base_defense >= 0),
    description     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS monster_template (
    id          VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    hp          INTEGER NOT NULL CHECK (hp > 0),
    attack      INTEGER NOT NULL CHECK (attack >= 0),
    defense     INTEGER NOT NULL CHECK (defense >= 0),
    xp_reward   INTEGER NOT NULL CHECK (xp_reward >= 0),
    gold_reward INTEGER NOT NULL CHECK (gold_reward >= 0),
    is_boss     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS item_template (
    id             VARCHAR(50) PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    item_type      VARCHAR(20) NOT NULL,
    attack_bonus   INTEGER NOT NULL DEFAULT 0 CHECK (attack_bonus >= 0),
    defense_bonus  INTEGER NOT NULL DEFAULT 0 CHECK (defense_bonus >= 0),
    value          INTEGER NOT NULL DEFAULT 0 CHECK (value >= 0)
);

-- Player accounts. password_hash and encrypted_email are both opaque
-- strings from the database's point of view -- Postgres never sees a
-- plaintext password or a plaintext email address.
CREATE TABLE IF NOT EXISTS player (
    id               UUID PRIMARY KEY,
    username         VARCHAR(20) UNIQUE NOT NULL,
    password_hash    VARCHAR(200) NOT NULL,
    encrypted_email  VARCHAR(500) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS player_character (
    id               UUID PRIMARY KEY,
    player_id        UUID NOT NULL REFERENCES player(id),
    name             VARCHAR(50) NOT NULL,
    character_class  VARCHAR(20) NOT NULL REFERENCES game_class(character_class),
    level            INTEGER NOT NULL DEFAULT 1,
    xp               INTEGER NOT NULL DEFAULT 0,
    gold             INTEGER NOT NULL DEFAULT 0,
    current_hp       INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS leaderboard_entry (
    id               BIGSERIAL PRIMARY KEY,
    character_name   VARCHAR(50) NOT NULL,
    score            INTEGER NOT NULL,
    recorded_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Append-only by convention: nothing in this application ever issues an
-- UPDATE or DELETE against audit_log.
CREATE TABLE IF NOT EXISTS audit_log (
    id            UUID PRIMARY KEY,
    event_type    VARCHAR(50) NOT NULL,
    actor_id      UUID,
    details       TEXT,
    occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Seed data matching src/main/resources/gamedata/*.csv exactly, so
-- switching GameDataRepository implementations changes nothing about
-- game balance.
INSERT INTO game_class (character_class, base_hp, base_attack, base_defense, description) VALUES
    ('WARRIOR', 30, 8, 5, 'A stalwart front-line fighter who trades finesse for raw durability.'),
    ('MAGE', 18, 12, 2, 'A fragile spellcaster whose attacks hit far harder than their armor suggests.'),
    ('ROGUE', 22, 10, 3, 'A quick striker who relies on precision over brute strength.'),
    ('CLERIC', 26, 7, 4, 'A resilient healer who can still hold their own in a fight.')
ON CONFLICT (character_class) DO NOTHING;

INSERT INTO monster_template (id, name, hp, attack, defense, xp_reward, gold_reward, is_boss) VALUES
    ('goblin', 'Goblin', 15, 4, 1, 20, 10, FALSE),
    ('skeleton', 'Skeleton', 20, 6, 2, 30, 15, FALSE),
    ('orc', 'Orc Brute', 28, 8, 3, 40, 20, FALSE),
    ('dire_wolf', 'Dire Wolf', 24, 7, 2, 35, 18, FALSE),
    ('dragon', 'Ember Dragon', 80, 15, 8, 250, 150, TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO item_template (id, name, item_type, attack_bonus, defense_bonus, value) VALUES
    ('rusty_sword', 'Rusty Sword', 'WEAPON', 2, 0, 10),
    ('iron_shield', 'Iron Shield', 'ARMOR', 0, 3, 15),
    ('leather_armor', 'Leather Armor', 'ARMOR', 0, 2, 8),
    ('minor_potion', 'Minor Potion', 'POTION', 0, 0, 5),
    ('flame_blade', 'Flame Blade', 'WEAPON', 5, 0, 40)
ON CONFLICT (id) DO NOTHING;
