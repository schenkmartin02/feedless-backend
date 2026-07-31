TRUNCATE champion_stats, rune_stats, item_stats, matchup_stats;

ALTER TABLE champion_stats
    ADD COLUMN platform VARCHAR(5) NOT NULL;

ALTER TABLE champion_stats
    DROP CONSTRAINT uq_champion_stats_patch_queue_champ_position_rank;

ALTER TABLE champion_stats
    ADD CONSTRAINT uq_champion_stats_dims
        UNIQUE (platform, patch, queue_id, champion_id, team_position,
                rank_tier);

ALTER TABLE rune_stats
    ADD COLUMN platform VARCHAR(5) NOT NULL;

ALTER TABLE rune_stats
    DROP CONSTRAINT uq_rune_stats_patch_queue_champ_position_rank_keystone;

ALTER TABLE rune_stats
    ADD CONSTRAINT uq_rune_stats_dims
        UNIQUE (platform, patch, queue_id, champion_id, team_position,
                rank_tier, keystone_id);

ALTER TABLE item_stats
    ADD COLUMN platform VARCHAR(5) NOT NULL;

ALTER TABLE item_stats
    DROP CONSTRAINT uq_item_stats_patch_queue_champ_position_item;

ALTER TABLE item_stats
    ADD CONSTRAINT uq_item_stats_dims
        UNIQUE (platform, patch, queue_id, champion_id, team_position,
                item_id);

ALTER TABLE matchup_stats
    ADD COLUMN platform VARCHAR(5) NOT NULL;

ALTER TABLE matchup_stats
    DROP CONSTRAINT uq_matchup_stats_patch_queue_champ_position_opponent;

ALTER TABLE matchup_stats
    ADD CONSTRAINT uq_matchup_stats_dims
        UNIQUE (platform, patch, queue_id, champion_id, team_position,
                opponent_champion_id);