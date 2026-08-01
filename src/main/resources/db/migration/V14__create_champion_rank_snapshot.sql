CREATE TABLE champion_rank_snapshot (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    snapshot_date DATE NOT NULL,
    platform VARCHAR(5) NOT NULL,
    patch VARCHAR(16) NOT NULL,
    queue_id INT NOT NULL,
    bracket VARCHAR(10) NOT NULL,
    team_position VARCHAR(8) NOT NULL,
    champion_id INT NOT NULL,
    rank_position INT NOT NULL,

    CONSTRAINT uq_champion_rank_snapshot_dims UNIQUE (snapshot_date, platform, patch, queue_id, bracket, team_position, champion_id)
);