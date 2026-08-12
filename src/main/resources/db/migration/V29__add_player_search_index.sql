CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE OR REPLACE FUNCTION f_unaccent(text)
    RETURNS text
AS
$$
SELECT unaccent('unaccent', $1)
$$ LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT;

CREATE INDEX idx_players_search
    ON players (platform, f_unaccent(lower(game_name)) text_pattern_ops);