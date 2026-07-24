CREATE TABLE players
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    puuid              VARCHAR(78) NOT NULL UNIQUE,
    game_name          VARCHAR(16),
    tag_line           VARCHAR(5),
    profile_icon_id    INT,
    profile_updated_at TIMESTAMPTZ
);