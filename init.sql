CREATE TABLE accounts (
    account_id SERIAL PRIMARY KEY,
    pin VARCHAR(255) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    account_id INTEGER,
    related_account_id INTEGER,
    transaction_type VARCHAR(20),
    amount DECIMAL(10, 2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

INSERT INTO accounts (pin, balance) VALUES
    ('$2a$10$Tblp0/5MtD4UrcWoLiLK0O8rEvfhXlOumv3YneptS0ynrNmfCI3eC', 1000.00),
    ('$2a$10$Tblp0/5MtD4UrcWoLiLK0O8rEvfhXlOumv3YneptS0ynrNmfCI3eC', 500.00),
    ('$2a$10$Tblp0/5MtD4UrcWoLiLK0O8rEvfhXlOumv3YneptS0ynrNmfCI3eC', 300.00);