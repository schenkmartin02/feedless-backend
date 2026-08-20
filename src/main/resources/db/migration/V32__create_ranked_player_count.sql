CREATE TABLE ranked_player_count
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    platform     VARCHAR(5)  NOT NULL,
    queue_type   VARCHAR(20) NOT NULL,
    player_count BIGINT      NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_ranked_player_count_dims UNIQUE (platform, queue_type)
);

INSERT INTO ranked_player_count (platform, queue_type, player_count)
SELECT p.platform, pr.queue_type, count(*)
FROM player_ranks pr
         JOIN players p ON p.id = pr.player_id
GROUP BY p.platform, pr.queue_type;