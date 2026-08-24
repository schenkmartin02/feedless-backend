CREATE TABLE match_timelines(
    match_id BIGINT PRIMARY KEY REFERENCES matches (id) ON DELETE CASCADE,
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);