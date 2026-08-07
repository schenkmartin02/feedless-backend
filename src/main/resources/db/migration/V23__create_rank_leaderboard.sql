CREATE TABLE rank_leaderboard
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    platform         VARCHAR(5)  NOT NULL,
    queue_type       VARCHAR(20) NOT NULL,
    rank_position    INT         NOT NULL,
    puuid            VARCHAR(78) NOT NULL,

    tier             VARCHAR(12) NOT NULL,
    league_points    INT         NOT NULL,
    wins             INT         NOT NULL,
    losses           INT         NOT NULL,

    kda              DOUBLE PRECISION,
    top_champion_ids INT[],
    delta            INT,

    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_rank_leaderboard_dims
        UNIQUE (platform, queue_type, rank_position)
)