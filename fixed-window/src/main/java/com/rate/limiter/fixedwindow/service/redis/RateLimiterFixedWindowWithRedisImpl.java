package com.rate.limiter.fixedwindow.service.redis;

import com.rate.limiter.fixedwindow.configs.RateLimiterFixedWindowWithRedis;

/**
 * Implementation of fixed window rate limiting using Redis.
 */
public class RateLimiterFixedWindowWithRedisImpl {
    private final RateLimiterFixedWindowWithRedis fixedWindowWithRedis;

    private RateLimiterFixedWindowWithRedisImpl(RateLimiterFixedWindowWithRedis fixedWindowWithRedis) {
        this.fixedWindowWithRedis = fixedWindowWithRedis;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if a request is allowed for a given key under the fixed window algorithm.
     *
     * @param key The unique identifier for the request.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean isAllowed(String key) {
        // Calculate the current window based on the window size
        long currentWindow = System.currentTimeMillis() / 1000 / fixedWindowWithRedis.getWindowSize();
        String redisKey = "rate:" + key + ":" + currentWindow;

        // Increment the count for the current window
        long currentCount = fixedWindowWithRedis.incrementKey(redisKey);

        // Return true if the current count is within the limit
        return currentCount <= fixedWindowWithRedis.getLimit();
    }

    /**
     * Builder for RateLimiterFixedWindowWithRedisImpl.
     */
    public static class Builder {
        private RateLimiterFixedWindowWithRedis fixedWindowWithRedis;

        /**
         * Sets the fixed window configuration for this implementation.
         *
         * @param fixedWindowWithRedis The configuration object.
         * @return The builder instance.
         */
        public Builder withFixedWindowWithRedis(RateLimiterFixedWindowWithRedis fixedWindowWithRedis) {
            this.fixedWindowWithRedis = fixedWindowWithRedis;
            return this;
        }

        /**
         * Builds the RateLimiterFixedWindowWithRedisImpl instance.
         *
         * @return A configured instance.
         */
        public RateLimiterFixedWindowWithRedisImpl build() {
            if (fixedWindowWithRedis == null) {
                throw new IllegalArgumentException("RateLimiterFixedWindowWithRedis is required");
            }
            return new RateLimiterFixedWindowWithRedisImpl(fixedWindowWithRedis);
        }
    }
}
