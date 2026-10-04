CREATE TABLE outbox_events (
                               id             BIGSERIAL PRIMARY KEY,
                               event_id       UUID NOT NULL UNIQUE,
                               aggregate_type VARCHAR(50) NOT NULL,
                               aggregate_id   VARCHAR(50) NOT NULL,
                               event_type     VARCHAR(50) NOT NULL,
                               payload        TEXT NOT NULL,
                               created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                               published_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (id) WHERE published_at IS NULL;