CREATE TABLE notifications (
                               id           BIGSERIAL PRIMARY KEY,
                               event_id     UUID         NOT NULL UNIQUE,
                               event_type   VARCHAR(50)  NOT NULL,
                               incident_id  BIGINT       NOT NULL,
                               service_name VARCHAR(100) NOT NULL,
                               severity     VARCHAR(10)  NOT NULL,
                               message      VARCHAR(500) NOT NULL,
                               occurred_at  TIMESTAMPTZ  NOT NULL,
                               created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);