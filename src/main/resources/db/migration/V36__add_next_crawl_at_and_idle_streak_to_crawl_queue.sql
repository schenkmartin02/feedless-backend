ALTER TABLE crawl_queue
    ADD COLUMN next_crawl_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN idle_streak   INT         NOT NULL DEFAULT 0;

UPDATE crawl_queue
SET next_crawl_at = last_crawled_at + interval '2 days'
WHERE last_crawled_at IS NOT NULL;

CREATE INDEX idx_crawl_queue_next_crawl
    ON crawl_queue (next_crawl_at)
    WHERE status = 'DONE';