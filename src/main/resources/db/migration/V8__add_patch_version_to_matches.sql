ALTER TABLE matches
    ADD COLUMN patch_major SMALLINT GENERATED ALWAYS AS (
        COALESCE(NULLIF(split_part(patch, '.', 1), ''), '0')::smallint
        ) STORED NOT NULL,

    ADD COLUMN patch_minor SMALLINT GENERATED ALWAYS AS (
        COALESCE(NULLIF(split_part(patch, '.', 2), ''), '0')::smallint
        ) STORED NOT NULL;
