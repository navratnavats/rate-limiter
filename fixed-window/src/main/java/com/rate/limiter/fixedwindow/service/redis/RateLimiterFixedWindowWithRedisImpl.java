package com.rate.limiter.fixedwindow.service.redis;


import com.rate.limiter.fixedwindow.configs.RateLimiterFixedWindowWithRedis;

public class RateLimiterFixedWindowWithRedisImpl {
    private final RateLimiterFixedWindowWithRedis fixedWindowWithRedis;
    private final Integer limit;

    private RateLimiterFixedWindowWithRedisImpl(RateLimiterFixedWindowWithRedis fixedWindowWithRedis, int limit) {
        this.fixedWindowWithRedis = fixedWindowWithRedis;
        this.limit = limit;
    }

    /**
     * Checks if a request is allowed for the given key.
     *
     * @param key The unique identifier for the user.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean isAllowed(String key) {
        long currentWindow = System.currentTimeMillis() / 1000 / fixedWindowWithRedis.getWindowSize();
        String redisKey = "rate:" + key + ":" + currentWindow;

        long currentCount = fixedWindowWithRedis.incrementKey(redisKey);
        return currentCount <= limit;
    }

    /**
     * Builder for RateLimiterFixedWindow.
     */
    public static class Builder {
        private RateLimiterFixedWindowWithRedis fixedWindowWithRedis;
        private Integer limit;

        public Builder withFixedWindowWithRedis(RateLimiterFixedWindowWithRedis fixedWindowWithRedis) {
            this.fixedWindowWithRedis = fixedWindowWithRedis;
            return this;
        }

        public Builder withLimit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public RateLimiterFixedWindowWithRedisImpl build() {
            if (fixedWindowWithRedis == null) {
                throw new IllegalArgumentException("RateLimiterFixedWindowWithRedis is required");
            }
            if(limit == null)
                throw new IllegalArgumentException("Limit is required");
            return new RateLimiterFixedWindowWithRedisImpl(fixedWindowWithRedis, limit);
        }
    }
}
