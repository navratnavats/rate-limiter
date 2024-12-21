package com.ratelimiter.slidingwindow.service;

import com.ratelimiter.common.constants.RedisServerMode;
import com.ratelimiter.common.models.RedisClient;
import com.ratelimiter.slidingwindow.configs.RateLimiterSlidingWindow;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * Implementation of sliding window rate limiting using Redis.
 */
public class RateLimiterSlidingWindowImpl {
    private final RateLimiterSlidingWindow rateLimiterSlidingWindow;

    /**
     * Constructor to initialize the RateLimiterSlidingWindow implementation.
     *
     * @param rateLimiterSlidingWindow The sliding window configuration object.
     */

    public RateLimiterSlidingWindowImpl(RateLimiterSlidingWindow rateLimiterSlidingWindow) {
        this.rateLimiterSlidingWindow = rateLimiterSlidingWindow;
    }

    /**
     * Checks if a request is allowed for a specific key under the sliding window rate limiting algorithm.
     *
     * @param rateLimiterSlidingWindow The sliding window configuration.
     * @param rateLimitBy              The key or identifier for the request.
     * @return true if the request is allowed, false otherwise.
     */

    public boolean isAllowed(RateLimiterSlidingWindow rateLimiterSlidingWindow, String rateLimitBy) {
        final int limit = rateLimiterSlidingWindow.getLimit();
        final int windowSizeMillis = rateLimiterSlidingWindow.getWindowSize() * rateLimiterSlidingWindow.getTimeUnit().getMilliValue();

        long currentTime = System.currentTimeMillis(); // Current time in milliseconds
        long windowStartTime = currentTime - windowSizeMillis;

        RedisClient redisClient = rateLimiterSlidingWindow.getRedisClient();
        String key = "rateLimit:" + rateLimitBy;

        if (redisClient.getRedisServerMode() == RedisServerMode.CLUSTER) {
            JedisCluster jedisCluster = redisClient.getJedisClusterClient();

            // Remove outdated requests
            jedisCluster.zremrangeByScore(key, 0, windowStartTime);

            // Count current requests
            long requestCount = jedisCluster.zcard(key);
            if (requestCount < limit) {
                // Add the new request
                jedisCluster.zadd(key, currentTime, String.valueOf(currentTime));
                // Set expiry in seconds
                jedisCluster.expire(key, windowSizeMillis / 1000);
                return true;
            }
            return false;

        } else { // For Standalone mode
            try (Jedis jedisClient = redisClient.getJedisPool().getResource()) {
                // Remove outdated requests
                jedisClient.zremrangeByScore(key, 0, windowStartTime);

                // Count current requests
                long requestCount = jedisClient.zcard(key);
                if (requestCount < limit) {
                    // Add the new request
                    jedisClient.zadd(key, currentTime, String.valueOf(currentTime));
                    // Set expiry in seconds
                    jedisClient.expire(key, windowSizeMillis / 1000);
                    return true;
                }
                return false;
            }
        }
    }
}
