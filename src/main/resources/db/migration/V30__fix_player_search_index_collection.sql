DROP INDEX idx_players_search;

CREATE INDEX idx_players_search
    ON players (platform, ((f_unaccent(lower(game_name))) COLLATE "C"));