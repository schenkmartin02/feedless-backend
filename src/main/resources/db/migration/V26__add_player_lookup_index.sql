CREATE INDEX idx_players_riot_id
    ON players (lower(game_name), lower(tag_line), platform);