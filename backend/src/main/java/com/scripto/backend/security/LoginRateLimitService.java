package com.scripto.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimitService {
    private static final long ENTRY_TTL_NANOS = Duration.ofMinutes(30).toNanos();

    private final Map<String, BucketEntry> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }

    public Bucket resolveBucket(String ipAddress) {
        BucketEntry entry = cache.computeIfAbsent(ipAddress, ignored -> new BucketEntry(createNewBucket()));
        entry.touch();
        return entry.bucket;
    }

    @Scheduled(fixedDelay = 600_000)
    void evictInactiveBuckets() {
        long now = System.nanoTime();
        cache.entrySet().removeIf(entry -> now - entry.getValue().lastAccessNanos > ENTRY_TTL_NANOS);
    }

    private static final class BucketEntry {
        private final Bucket bucket;
        private volatile long lastAccessNanos;

        private BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            touch();
        }

        private void touch() {
            this.lastAccessNanos = System.nanoTime();
        }
    }
}
