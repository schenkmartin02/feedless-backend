CREATE TABLE participants
(
    id                              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    match_id                        BIGINT     NOT NULL REFERENCES matches (id),
    player_id                       BIGINT     NOT NULL REFERENCES players (id),
    champion_id                     INT        NOT NULL,
    team_position                   VARCHAR(8) NOT NULL,
    kills                           INT        NOT NULL,
    deaths                          INT        NOT NULL,
    assists                         INT        NOT NULL,
    win                             BOOLEAN    NOT NULL,

    -- Új Riot API statisztikai mezők
    baron_kills                     INT        NOT NULL,
    champ_level                     INT        NOT NULL,
    double_kills                    INT        NOT NULL,
    dragon_kills                    INT        NOT NULL,
    first_blood_kill                BOOLEAN    NOT NULL,
    first_tower_kill                BOOLEAN    NOT NULL,
    game_ended_in_surrender         BOOLEAN    NOT NULL,
    gold_earned                     INT        NOT NULL,
    gold_spent                      INT        NOT NULL,
    inhibitor_take_downs            INT        NOT NULL,
    item0                           INT        NOT NULL,
    item1                           INT        NOT NULL,
    item2                           INT        NOT NULL,
    item3                           INT        NOT NULL,
    item4                           INT        NOT NULL,
    item5                           INT        NOT NULL,
    item6                           INT        NOT NULL,
    neutral_minions_killed          INT        NOT NULL,
    penta_kills                     INT        NOT NULL,
    quadra_kills                    INT        NOT NULL,
    summoner1_id                    INT        NOT NULL,
    summoner2_id                    INT        NOT NULL,
    team_id                         INT        NOT NULL,
    total_damage_dealt_to_champions INT        NOT NULL,
    total_heal                      INT        NOT NULL,
    total_minions_killed            INT        NOT NULL,
    triple_kills                    INT        NOT NULL,
    vision_score                    INT        NOT NULL,

    -- Kiemelt rúna mezők a villámgyors aggregációhoz (GROUP BY-hoz)
    primary_style_id                INT        NOT NULL,
    sub_style_id                    INT        NOT NULL,
    keystone_id                     INT        NOT NULL,

    -- A teljes részletes struktúra meccs-részletek nézethez
    perks                           JSONB      NOT NULL,

    CONSTRAINT uq_participants_match_player UNIQUE (match_id, player_id)
);

-- Indexek az aggregációk és lekérdezések gyorsítására
CREATE INDEX idx_participants_player_id ON participants (player_id);
