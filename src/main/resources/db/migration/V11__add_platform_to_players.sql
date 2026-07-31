ALTER TABLE players
    ADD COLUMN platform VARCHAR(5);

UPDATE players p
SET platform = sub.platform
FROM (
    SELECT DISTINCT ON (pa.player_id)
           pa.player_id,
           m.platform
    FROM participants pa
    JOIN matches m ON m.id = pa.match_id
    ORDER BY pa.player_id, m.id DESC
) sub
WHERE p.id = sub.player_id;