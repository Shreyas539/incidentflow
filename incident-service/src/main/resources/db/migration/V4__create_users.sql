CREATE TABLE app_users (
                           id            BIGSERIAL PRIMARY KEY,
                           username      VARCHAR(50)  NOT NULL UNIQUE,
                           password_hash VARCHAR(100) NOT NULL,
                           role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','RESPONDER','VIEWER')),
                           created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);