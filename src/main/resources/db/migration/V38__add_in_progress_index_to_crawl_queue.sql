CREATE INDEX idx_crawl_queue_in_progress
    ON crawl_queue (started_at)
    WHERE status = 'IN_PROGRESS';
