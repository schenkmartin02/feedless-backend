CREATE TABLE match_teams (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    team_id INT NOT NULL,
    win BOOLEAN NOT NULL,
    barons INT NOT NULL,
    dragons INT NOT NULL,
    heralds INT NOT NULL,
    towers INT NOT NULL,
    inhibitors INT NOT NULL,

    CONSTRAINT uq_match_teams_dims UNIQUE (match_id, team_id)
);

ALTER TABLE participants
    ADD COLUMN total_damage_taken INT,
    ADD COLUMN wards_placed INT,
    ADD COLUMN summoner_level INT;
