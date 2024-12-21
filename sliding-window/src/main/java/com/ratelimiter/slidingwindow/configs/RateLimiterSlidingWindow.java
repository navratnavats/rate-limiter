package com.ratelimiter.slidingwindow.configs;

import com.ratelimiter.common.constants.TimeUnit;
import com.ratelimiter.common.models.RedisClient;

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

    // Getters
    public RedisClient getRedisClient() {
        return redisClient;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public int getWindowSize() {
        return windowSize;
    }

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

    public static class RateLimiterSlidingWindowBuilder {
        private TimeUnit timeUnit;
        private int windowSize;
        private int limit;
        private RedisClient redisClient;

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
