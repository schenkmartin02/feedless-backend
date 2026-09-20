CREATE TABLE champion_ban_scope
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    platform      VARCHAR(5)  NOT NULL,
    patch         VARCHAR(16) NOT NULL,
    queue_id      INT         NOT NULL,
    rank_tier     VARCHAR(12) NOT NULL,

    total_matches BIGINT      NOT NULL,

    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_champion_ban_scope_dims
        UNIQUE (platform, patch, queue_id, rank_tier)
);
