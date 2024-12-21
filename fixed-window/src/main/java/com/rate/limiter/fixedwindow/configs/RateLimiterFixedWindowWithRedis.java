package com.rate.limiter.fixedwindow.configs;

import com.ratelimiter.common.models.RedisClient;
import com.ratelimiter.common.constants.RedisServerMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * Configuration class for fixed window rate limiting using Redis.
 */
public class RateLimiterFixedWindowWithRedis {
    private final RedisClient redisClient;
    private final int windowSize; // in seconds
    private final int limit;

    public RateLimiterFixedWindowWithRedis(RedisClient redisClient, int windowSize, int limit) {
        this.redisClient = redisClient;
        this.windowSize = windowSize;
        this.limit = limit;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * Increments the request count for the given Redis key.
     *
     * @param redisKey The key associated with the request.
     * @return The updated request count.
     */
    public long incrementKey(String redisKey) {
        try {
            if (redisClient.getRedisServerMode() == RedisServerMode.CLUSTER) {
                return handleClusterIncrement(redisKey);
            } else {
                return handleStandaloneIncrement(redisKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in rate limiter: " + e.getMessage(), e);
        }
    }

    private long handleClusterIncrement(String redisKey) {
        JedisCluster cluster = redisClient.getJedisClusterClient();
        long count = cluster.incr(redisKey);
        if (count == 1) {
            cluster.expire(redisKey, windowSize); // Set expiry for new keys
        }
        return count;
    }

    private long handleStandaloneIncrement(String redisKey) {
        try (Jedis jedis = redisClient.getJedisPool().getResource()) {
            long count = jedis.incr(redisKey);
            if (count == 1) {
                jedis.expire(redisKey, windowSize); // Set expiry for new keys
            }
            return count;
        }
    }
}
