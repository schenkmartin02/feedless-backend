ALTER TABLE matches ADD COLUMN aggregated_at TIMESTAMPTZ;

CREATE INDEX idx_matches_pending_aggregation
    ON matches (id)
    WHERE aggregated_at IS NULL;