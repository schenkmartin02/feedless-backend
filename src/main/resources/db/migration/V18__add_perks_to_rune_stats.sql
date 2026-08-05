TRUNCATE rune_stats;

ALTER TABLE rune_stats
    ADD COLUMN primary_perk_2 INT NOT NULL,
    ADD COLUMN primary_perk_3 INT NOT NULL,
    ADD COLUMN primary_perk_4 INT NOT NULL,
    ADD COLUMN sub_perk_1 INT NOT NULL,
    ADD COLUMN sub_perk_2 INT NOT NULL;

ALTER TABLE rune_stats
    DROP CONSTRAINT uq_rune_stats_dims;

ALTER TABLE rune_stats
    ADD CONSTRAINT uq_rune_stats_dims
        UNIQUE (platform, patch, queue_id, champion_id, team_position,
                rank_tier, keystone_id, primary_perk_2, primary_perk_3, primary_perk_4, sub_perk_1, sub_perk_2);