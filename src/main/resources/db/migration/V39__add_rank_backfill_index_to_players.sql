CREATE INDEX idx_players_rank_backfill
    ON players (platform, ranks_checked_at NULLS FIRST);
