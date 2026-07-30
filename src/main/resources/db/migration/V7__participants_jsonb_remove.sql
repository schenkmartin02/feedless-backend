ALTER TABLE participants
    ADD COLUMN stat_perk_defense INT,
    ADD COLUMN stat_perk_flex INT,
    ADD COLUMN stat_perk_offense INT,
    ADD COLUMN primary_perk_2 INT,
    ADD COLUMN primary_perk_3 INT,
    ADD COLUMN primary_perk_4 INT,
    ADD COLUMN sub_perk_1 INT,
    ADD COLUMN sub_perk_2 INT;

UPDATE participants
SET
    -- Stat Perks (Kicsi rúnák az objektumból)
    stat_perk_defense = (perks->'statPerks'->>'defense')::int,
    stat_perk_flex    = (perks->'statPerks'->>'flex')::int,
    stat_perk_offense = (perks->'statPerks'->>'offense')::int,

    -- Primary Style Selection 2, 3, 4 (Az első stílus [0] 1., 2., 3. indexű rúnái)
    -- (Emlékeztetőül: a 0. indexű selection a keystone_id, ami már ki van gyűjtve)
    primary_perk_2    = (perks->'styles'->0->'selections'->1->>'perk')::int,
    primary_perk_3    = (perks->'styles'->0->'selections'->2->>'perk')::int,
    primary_perk_4    = (perks->'styles'->0->'selections'->3->>'perk')::int,

    -- Sub Style Selection 1, 2 (A másodlagos stílus [1] 0. és 1. indexű rúnái)
    sub_perk_1        = (perks->'styles'->1->'selections'->0->>'perk')::int,
    sub_perk_2        = (perks->'styles'->1->'selections'->1->>'perk')::int
WHERE perks IS NOT NULL;

ALTER TABLE participants
    ALTER COLUMN stat_perk_defense SET NOT NULL,
    ALTER COLUMN stat_perk_flex SET NOT NULL,
    ALTER COLUMN stat_perk_offense SET NOT NULL,
    ALTER COLUMN primary_perk_2 SET NOT NULL,
    ALTER COLUMN primary_perk_3 SET NOT NULL,
    ALTER COLUMN primary_perk_4 SET NOT NULL,
    ALTER COLUMN sub_perk_1 SET NOT NULL,
    ALTER COLUMN sub_perk_2 SET NOT NULL;

ALTER TABLE participants
    DROP COLUMN perks;