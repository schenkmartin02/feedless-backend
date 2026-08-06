CREATE TABLE champion_ban_snapshot
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    snapshot_date DATE        NOT NULL,
    platform      VARCHAR(5)  NOT NULL,
    patch         VARCHAR(16) NOT NULL,
    queue_id      INT         NOT NULL,
    bracket       VARCHAR(10) NOT NULL,
    champion_id   INT         NOT NULL,

    bans          BIGINT      NOT NULL,
    total_matches BIGINT      NOT NULL,

    CONSTRAINT uq_champion_ban_snapshot_dims
        UNIQUE (snapshot_date, platform, patch, queue_id, bracket,
                champion_id)
);