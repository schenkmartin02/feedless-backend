ALTER TABLE crawl_queue
    ADD COLUMN platform VARCHAR(5);

UPDATE crawl_queue c
SET platform = p.platform
FROM players p
WHERE p.puuid = c.puuid
  AND p.platform IS NOT NULL;

DELETE FROM crawl_queue
WHERE platform IS NULL;

ALTER TABLE crawl_queue
    ALTER COLUMN platform SET NOT NULL;

DROP INDEX idx_worker_pending_tasks;

CREATE INDEX idx_worker_pending_tasks
    ON crawl_queue (platform, priority DESC, last_crawled_at ASC NULLS FIRST)
    WHERE status = 'PENDING';