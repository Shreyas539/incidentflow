CREATE TABLE incidents (
                           id          BIGSERIAL PRIMARY KEY,
                           title       VARCHAR(200) NOT NULL,
                           description TEXT,
                           severity    VARCHAR(10)  NOT NULL CHECK (severity IN ('SEV1','SEV2','SEV3','SEV4')),
                           status      VARCHAR(20)  NOT NULL CHECK (status IN ('OPEN','ACKNOWLEDGED','RESOLVED')),
                           service_id  BIGINT NOT NULL REFERENCES monitored_services(id),
                           created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_service_id ON incidents(service_id);
CREATE INDEX idx_incidents_status ON incidents(status);