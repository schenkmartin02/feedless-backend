ALTER TABLE players
    ALTER COLUMN ranks_checked_at SET DEFAULT '-infinity';

UPDATE players
SET ranks_checked_at = '-infinity'
WHERE ranks_checked_at IS NULL;
