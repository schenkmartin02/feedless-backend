CREATE TABLE player_ranks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    player_id BIGINT NOT NULL REFERENCES players(id),
    queue_type VARCHAR(20) NOT NULL,
    tier VARCHAR(12) NOT NULL,
    division VARCHAR(4) NOT NULL,
    league_points INT NOT NULL,
    wins INT NOT NULL,
    losses INT NOT NULL,
    CONSTRAINT uq_player_ranks_player_queue UNIQUE (player_id, queue_type)
);