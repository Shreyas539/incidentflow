ALTER TABLE incidents ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE incidents ADD COLUMN acknowledged_at TIMESTAMPTZ;
ALTER TABLE incidents ADD COLUMN resolved_at TIMESTAMPTZ;

CREATE TABLE incident_audit_logs (
                                     id          BIGSERIAL PRIMARY KEY,
                                     incident_id BIGINT NOT NULL REFERENCES incidents(id),
                                     from_status VARCHAR(20),
                                     to_status   VARCHAR(20) NOT NULL,
                                     note        VARCHAR(500),
                                     changed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_incident_id ON incident_audit_logs(incident_id);