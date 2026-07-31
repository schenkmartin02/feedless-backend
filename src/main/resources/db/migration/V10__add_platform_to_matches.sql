ALTER TABLE matches
    ADD COLUMN platform VARCHAR(5)
        GENERATED ALWAYS AS (split_part(match_id, '_', 1)) STORED NOT NULL;