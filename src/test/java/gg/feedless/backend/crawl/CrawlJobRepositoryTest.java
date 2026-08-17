package gg.feedless.backend.crawl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class CrawlJobRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    CrawlJobRepository crawlJobRepository;

    @Test
    void enqueue() {
        int result = crawlJobRepository.enqueue("test2", 0, "EUN1");
        int result2 = crawlJobRepository.enqueue("test2", 0, "EUN1");
        assertEquals(1, result);
        assertEquals(0, result2);
    }

    @Test
    void claimNextJob() {
        CrawlJob testJob = new CrawlJob("test", 0, "EUN1");
        testJob.setStatus(CrawlStatus.IN_PROGRESS);
        crawlJobRepository.save(testJob);

        CrawlJob testJob2 = new CrawlJob("test2", 0, "EUN1");
        crawlJobRepository.save(testJob2);

        Optional<CrawlJob> result = crawlJobRepository.claimNextJob("EUN1");
        assertEquals("test2", result.get().getPuuid());
    }

    @Test
    void findByPuuid() {
        CrawlJob testJob = new CrawlJob("test", 0, "EUN1");
        testJob.setStatus(CrawlStatus.IN_PROGRESS);
        crawlJobRepository.save(testJob);

        Optional<CrawlJob> result = crawlJobRepository.findByPuuid("test");
        assertEquals(CrawlStatus.IN_PROGRESS, result.get().getStatus());
    }

    @Test
    void claimNextJobForPriority() {
        CrawlJob testJob = new CrawlJob("test", 0, "EUN1");
        crawlJobRepository.save(testJob);

        CrawlJob testJob2 = new CrawlJob("test2", 2, "EUN1");
        crawlJobRepository.save(testJob2);

        Optional<CrawlJob> result = crawlJobRepository.claimNextJob("EUN1");
        assertEquals("test2", result.get().getPuuid());
    }

    @Test
    void recoveryTest() {
        CrawlJob testJob = new CrawlJob("recovery", 0, "EUN1");
        testJob.setStatus(CrawlStatus.IN_PROGRESS);
        testJob.setStartedAt(OffsetDateTime.now().minusMinutes(30));
        crawlJobRepository.save(testJob);

        CrawlJob testJob2 = new CrawlJob("recovery2", 0, "EUN1");
        testJob2.setStatus(CrawlStatus.IN_PROGRESS);
        testJob2.setStartedAt(OffsetDateTime.now().minusMinutes(5));
        crawlJobRepository.save(testJob2);

        int resultSum = crawlJobRepository.recovery(OffsetDateTime.now().minusMinutes(20), 5);
        Optional<CrawlJob> result = crawlJobRepository.findByPuuid("recovery");
        Optional<CrawlJob> result2 = crawlJobRepository.findByPuuid("recovery2");

        assertEquals(1, resultSum);
        assertEquals(CrawlStatus.PENDING, result.get().getStatus());
        assertNull(result.get().getStartedAt());
        assertEquals(CrawlStatus.IN_PROGRESS, result2.get().getStatus());

    }

    @Test
    void scheduleRecrawl() {
        CrawlJob testJob = new CrawlJob("recovery", 0, "EUN1");
        testJob.setStatus(CrawlStatus.DONE);
        crawlJobRepository.save(testJob);

        CrawlJob testJob2 = new CrawlJob("recovery2", 0, "EUN1");
        testJob2.setStatus(CrawlStatus.DONE);
        testJob2.setLastCrawledAt(OffsetDateTime.now().minusDays(2));
        crawlJobRepository.save(testJob2);

        CrawlJob testJob3 = new CrawlJob("recovery3", 0, "EUN1");
        testJob3.setStatus(CrawlStatus.DONE);
        testJob3.setLastCrawledAt(OffsetDateTime.now().minusDays(2));
        crawlJobRepository.save(testJob3);

        int resultSum = crawlJobRepository.scheduleRecrawl(OffsetDateTime.now().minusDays(1), 1);
        Optional<CrawlJob> result = crawlJobRepository.findByPuuid("recovery");
        Optional<CrawlJob> result2 = crawlJobRepository.findByPuuid("recovery2");
        Optional<CrawlJob> result3 = crawlJobRepository.findByPuuid("recovery3");

        assertEquals(1, resultSum);
        assertEquals(CrawlStatus.DONE, result.get().getStatus());
        assertNull(result.get().getStartedAt());
        assertEquals(CrawlStatus.PENDING, result2.get().getStatus());
        assertEquals(0, result3.get().getPriority());
    }
}