CREATE TABLE participants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES matches(id),
    player_id BIGINT NOT NULL REFERENCES players(id),
    champion_id INT NOT NULL,
    team_position VARCHAR(8) NOT NULL,
    kills INT NOT NULL,
    deaths INT NOT NULL,
    assists INT NOT NULL,
    win BOOLEAN NOT NULL,
    CONSTRAINT uq_participants_match_player UNIQUE (match_id, player_id)
);

CREATE INDEX idx_participants_player_id ON participants (player_id);