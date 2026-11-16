CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY,
    customer VARCHAR(255) NOT NULL,
    item VARCHAR(255) NOT NULL,
    amount_cents INT NOT NULL
);
