package platform.ecommerce.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test configuration for Rate Limiter.
 * Creates isolated rate limiters for each test to prevent state leakage.
 */
@TestConfiguration
public class TestRateLimiterConfig {

    private static final int TEST_LIMIT = 2;
    private static final Duration TEST_REFRESH_PERIOD = Duration.ofSeconds(1);

    @Bean
    @Primary
    public TestableRateLimiterRegistry testRateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitForPeriod(TEST_LIMIT)
                .limitRefreshPeriod(TEST_REFRESH_PERIOD)
                .timeoutDuration(Duration.ZERO)
                .build();

        // Custom registry that creates fresh rate limiters
        return new TestableRateLimiterRegistry(defaultConfig);
    }

    /**
     * Custom RateLimiterRegistry that can reset rate limiters for test isolation.
     */
    public static class TestableRateLimiterRegistry extends InMemoryRateLimiterRegistry {

        private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
        private final RateLimiterConfig config;

        public TestableRateLimiterRegistry(RateLimiterConfig config) {
            super(config);
            this.config = config;
        }

        @Override
        public @NonNull RateLimiter rateLimiter(@NonNull String name) {
            return rateLimiters.computeIfAbsent(name,
                    n -> RateLimiter.of(n, config));
        }

        @Override
        public @NonNull RateLimiter rateLimiter(@NonNull String name, @NonNull RateLimiterConfig config) {
            return rateLimiters.computeIfAbsent(name,
                    n -> RateLimiter.of(n, config));
        }

        /**
         * Reset all rate limiters for test isolation.
         */
        public void resetAll() {
            rateLimiters.forEach((name, limiter) -> {
                limiter.changeLimitForPeriod(config.getLimitForPeriod());
                limiter.drainPermissions();
                limiter.changeLimitForPeriod(config.getLimitForPeriod());
            });
        }

        /**
         * Clear all rate limiters and create fresh instances.
         */
        public void clearAll() {
            rateLimiters.clear();
        }
    }
}
