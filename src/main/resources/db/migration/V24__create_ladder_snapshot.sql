CREATE TABLE ladder_snapshot
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    snapshot_date DATE        NOT NULL,
    platform      VARCHAR(5)  NOT NULL,
    queue_type    VARCHAR(20) NOT NULL,
    puuid         VARCHAR(78) NOT NULL,
    rank_position INT         NOT NULL,

    CONSTRAINT uq_ladder_snapshot_dims
        UNIQUE (snapshot_date, platform, queue_type, puuid)
);