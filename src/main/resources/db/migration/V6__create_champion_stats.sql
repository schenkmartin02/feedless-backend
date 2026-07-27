CREATE TABLE champion_stats (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    patch VARCHAR(16) NOT NULL,
    queue_id INT NOT NULL,
    champion_id INT NOT NULL,
    team_position VARCHAR(8) NOT NULL,
    rank_tier VARCHAR(12) NOT NULL,

    games INT NOT NULL,
    wins INT NOT NULL,
    sum_kills BIGINT NOT NULL,
    sum_deaths BIGINT NOT NULL,
    sum_assists BIGINT NOT NULL,
    sum_gold_earned BIGINT NOT NULL,
    sum_cs BIGINT NOT NULL,
    sum_damage_to_champions BIGINT NOT NULL,
    sum_vision_score BIGINT NOT NULL,
    sum_duration BIGINT NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_champion_stats_patch_queue_champ_position_rank UNIQUE (patch, queue_id, champion_id, team_position, rank_tier)
);