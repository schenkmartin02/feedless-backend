CREATE TABLE rune_stats (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            patch VARCHAR(16) NOT NULL,
                            queue_id INT NOT NULL,
                            champion_id INT NOT NULL,
                            team_position VARCHAR(8) NOT NULL,
                            rank_tier VARCHAR(12) NOT NULL,
                            keystone_id INT NOT NULL,
                            games INT NOT NULL,
                            wins INT NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                            CONSTRAINT uq_rune_stats_patch_queue_champ_position_rank_keystone UNIQUE (patch, queue_id, champion_id, team_position, rank_tier, keystone_id)
);

CREATE TABLE item_stats (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            patch VARCHAR(16) NOT NULL,
                            queue_id INT NOT NULL,
                            champion_id INT NOT NULL,
                            team_position VARCHAR(8) NOT NULL,
                            item_id INT NOT NULL,
                            games INT NOT NULL,
                            wins INT NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                            CONSTRAINT uq_item_stats_patch_queue_champ_position_item UNIQUE (patch, queue_id, champion_id, team_position, item_id)
);

CREATE TABLE matchup_stats (
                               id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               patch VARCHAR(16) NOT NULL,
                               queue_id INT NOT NULL,
                               team_position VARCHAR(8) NOT NULL,
                               champion_id INT NOT NULL,
                               opponent_champion_id INT NOT NULL,
                               games INT NOT NULL,
                               wins INT NOT NULL,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                               CONSTRAINT uq_matchup_stats_patch_queue_champ_position_opponent UNIQUE (patch, queue_id, champion_id, team_position, opponent_champion_id)
);