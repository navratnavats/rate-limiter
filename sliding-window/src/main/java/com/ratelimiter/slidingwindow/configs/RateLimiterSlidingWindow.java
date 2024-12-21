package com.ratelimiter.slidingwindow.configs;

import com.ratelimiter.common.constants.TimeUnit;
import com.ratelimiter.common.models.RedisClient;

/**
 * Configuration class for the sliding window rate limiter.
 */

public class RateLimiterSlidingWindow {
    private final RedisClient redisClient;
    private final TimeUnit timeUnit;
    private final int windowSize;
    private final int limit;

    private RateLimiterSlidingWindow(RedisClient redisClient, TimeUnit timeUnit, int windowSize, int limit) {
        this.redisClient = redisClient;
        this.timeUnit = timeUnit;
        this.windowSize = windowSize;
        this.limit = limit;
    }

    /**
     * @return The Redis client associated with this configuration.
     */
    public RedisClient getRedisClient() {
        return redisClient;
    }

    /**
     * @return The time unit of the sliding window.
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * @return The size of the sliding window.
     */
    public int getWindowSize() {
        return windowSize;
    }

    /**
     * @return The maximum allowed requests in the sliding window.
     */
    public int getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "RateLimiterSlidingWindow{" +
                "redisClient=" + redisClient +
                ", timeUnit=" + timeUnit +
                ", windowSize=" + windowSize +
                ", limit=" + limit +
                '}';
    }

    /**
     * Builder class to construct a RateLimiterSlidingWindow instance.
     */
    public static class RateLimiterSlidingWindowBuilder {
        private TimeUnit timeUnit;
        private int windowSize;
        private int limit;
        private RedisClient redisClient;

        /**
         * Sets the Redis client for this configuration.
         *
         * @param redisClient The Redis client.
         * @return The builder instance.
         */
        public RateLimiterSlidingWindowBuilder redisClient(RedisClient redisClient) {
            this.redisClient = redisClient;
            return this;
        }

        public RateLimiterSlidingWindowBuilder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        public RateLimiterSlidingWindowBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public RateLimiterSlidingWindowBuilder timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public RateLimiterSlidingWindow build() {
            return new RateLimiterSlidingWindow(redisClient, timeUnit, windowSize, limit);
        }
    }
}
