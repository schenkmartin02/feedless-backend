UPDATE players SET profile_icon_id = 0 WHERE profile_icon_id IS NULL;

ALTER TABLE players
    ALTER COLUMN profile_icon_id SET DEFAULT 0,
    ALTER COLUMN profile_icon_id SET NOT NULL;