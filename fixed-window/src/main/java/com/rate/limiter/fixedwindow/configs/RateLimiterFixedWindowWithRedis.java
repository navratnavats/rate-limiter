package com.rate.limiter.fixedwindow.configs;

import com.ratelimiter.common.models.RedisClient;
import com.ratelimiter.common.constants.RedisServerMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class RateLimiterFixedWindowWithRedis {

    private final RedisClient redisClient;
    private final int windowSize;
    private final RedisServerMode redisServerMode;

    public RateLimiterFixedWindowWithRedis(RedisClient redisClient, int windowSize) {
        this.redisClient = redisClient;
        this.windowSize = windowSize;
        this.redisServerMode = redisClient.getRedisServerMode();
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public RedisServerMode getRedisServerMode() {
        return redisServerMode;
    }

    public long incrementKey(String redisKey) {
        if (redisServerMode == RedisServerMode.CLUSTER) {
            JedisCluster jedisCluster = redisClient.getJedisClusterClient();
            return incrementInCluster(jedisCluster, redisKey);
        } else {
            Jedis jedis = redisClient.getJedisClient();
            return incrementInStandalone(jedis, redisKey);
        }
    }

    private long incrementInCluster(JedisCluster jedisCluster, String redisKey) {
        long currentCount = jedisCluster.incr(redisKey);
        if (currentCount == 1) {
            jedisCluster.expire(redisKey, windowSize);
        }
        return currentCount;
    }

    private long incrementInStandalone(Jedis jedis, String redisKey) {
        long currentCount = jedis.incr(redisKey);
        if (currentCount == 1) {
            jedis.expire(redisKey, windowSize);
        }
        return currentCount;
    }
}
