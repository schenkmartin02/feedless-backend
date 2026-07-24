CREATE TABLE crawl_queue
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    puuid           VARCHAR(78) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL CHECK ( status IN ('PENDING', 'IN_PROGRESS', 'DONE', 'ERROR') ) DEFAULT 'PENDING',
    priority        INT         NOT NULL                                                                 DEFAULT 0,
    last_crawled_at TIMESTAMPTZ,
    retry_counter   INT         NOT NULL                                                                 DEFAULT 0,
    started_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL                                                                 DEFAULT now()
);

CREATE INDEX idx_worker_pending_tasks ON crawl_queue (priority DESC, last_crawled_at ASC NULLS FIRST) WHERE status = 'PENDING';