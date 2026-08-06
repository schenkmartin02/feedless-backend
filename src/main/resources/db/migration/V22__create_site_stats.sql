CREATE TABLE site_stats (
    id INT PRIMARY KEY,
    analyzed_matches BIGINT NOT NULL,
    tracked_players BIGINT NOT NULL,
    champion_count INT NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT site_stats_single_row CHECK (id = 1)
);

INSERT INTO site_stats (id, analyzed_matches, tracked_players, champion_count)
SELECT 1,
       (SELECT count(*) FROM matches WHERE aggregated_at IS NOT NULL),
       (SELECT count(*) FROM players),
       (SELECT count(DISTINCT champion_id) FROM champion_stats);