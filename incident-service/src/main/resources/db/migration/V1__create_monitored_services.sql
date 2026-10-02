CREATE TABLE monitored_services (
                                    id          BIGSERIAL PRIMARY KEY,
                                    name        VARCHAR(100) NOT NULL UNIQUE,
                                    description VARCHAR(500),
                                    owner_team  VARCHAR(100),
                                    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);