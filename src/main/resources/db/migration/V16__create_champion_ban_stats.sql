CREATE TABLE champion_ban_stats
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    platform      VARCHAR(5)  NOT NULL,
    patch         VARCHAR(16) NOT NULL,
    queue_id      INT         NOT NULL,
    champion_id   INT         NOT NULL,
    rank_tier     VARCHAR(12) NOT NULL,

    bans          BIGINT      NOT NULL,
    total_matches BIGINT      NOT NULL,

    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_champion_ban_stats_dims
        UNIQUE (platform, patch, queue_id, champion_id, rank_tier)
);