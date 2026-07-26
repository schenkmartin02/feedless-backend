package gg.feedless.backend.crawl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {
    @Modifying
    @Query(value = """
        INSERT INTO crawl_queue (puuid, priority)
        VALUES (:puuid, :priority)
        ON CONFLICT (puuid) DO NOTHING
        """, nativeQuery = true)
    int enqueue(
            @Param("puuid") String puuid,
            @Param("priority") int priority
    );

    //RETURNING miatt result-set query, az inspection false positive
    @SuppressWarnings("SpringDataModifyingAnnotationMissing")
    @Query(value = """
            UPDATE crawl_queue
            SET status = 'IN_PROGRESS', started_at = now()
            WHERE id = (SELECT id FROM crawl_queue WHERE status = 'PENDING'
                ORDER BY priority DESC, last_crawled_at NULLS FIRST LIMIT 1 FOR UPDATE SKIP LOCKED)
            RETURNING *
            """, nativeQuery = true)
    Optional<CrawlJob> claimNextJob();

    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE crawl_queue
        SET status = 'PENDING', started_at = null
        WHERE status = 'IN_PROGRESS' AND started_at < :cutoff
        """, nativeQuery = true)
    int recovery(@Param("cutoff") OffsetDateTime cutoff);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE crawl_queue
            SET status = 'PENDING', priority = 1, started_at = null
            WHERE status = 'DONE' AND last_crawled_at < :cutoff AND id IN (SELECT id FROM crawl_queue WHERE status = 'DONE' AND last_crawled_at < :cutoff
                    ORDER BY last_crawled_at LIMIT :batchSize)
            """, nativeQuery = true)
    int scheduleRecrawl(@Param("cutoff") OffsetDateTime cutoff, @Param("batchSize") int batchSize);

    Optional<CrawlJob> findByPuuid(String puuid);
}
