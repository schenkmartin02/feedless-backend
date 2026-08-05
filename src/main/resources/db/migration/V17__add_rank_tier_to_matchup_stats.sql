TRUNCATE matchup_stats;

ALTER TABLE matchup_stats
    ADD COLUMN rank_tier VARCHAR(12) NOT NULL;

ALTER TABLE matchup_stats
    DROP CONSTRAINT uq_matchup_stats_dims;

ALTER TABLE matchup_stats
    ADD CONSTRAINT uq_matchup_stats_dims
        UNIQUE (patch, queue_id, champion_id, team_position, opponent_champion_id, platform,
                rank_tier);