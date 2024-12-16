package com.ratelimiter.common.models;

import com.ratelimiter.common.constants.Constants;
import com.ratelimiter.common.constants.RedisServerMode;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisClient {
    private final List<String> nodes;
    private final Integer connectionTimeout;
    private final Integer socketTimeout;
    private final RedisServerMode redisServerMode;
    private final Integer maxAttempts;
    private Jedis jedisClient;          // For Standalone mode
    private JedisCluster jedisClusterClient; // For Cluster mode
    private JedisPool jedisPool;        // Optional: Pool for Standalone

    private RedisClient(List<String> nodes, Integer connectionTimeout, Integer socketTimeout,
                        RedisServerMode redisServerMode, Integer maxAttempts) {
        if (redisServerMode == null) {
            throw new IllegalArgumentException("RedisServerMode must not be null");
        }

        this.nodes = nodes;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.redisServerMode = redisServerMode;
        this.maxAttempts = maxAttempts;

        if (redisServerMode == RedisServerMode.CLUSTER) {
            createCluster(nodes);
        } else if (redisServerMode == RedisServerMode.STANDALONE) {
            createStandaloneInstance(nodes);
        } else {
            throw new IllegalArgumentException("Unsupported RedisServerMode: " + redisServerMode);
        }
    }

    private void createCluster(List<String> nodes) {
        try {
            Set<HostAndPort> jedisNodes = new HashSet<>();
            for (String node : nodes) {
                String[] hostPort = node.split(":");
                jedisNodes.add(new HostAndPort(hostPort[0], Integer.parseInt(hostPort[1])));
            }

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxWaitMillis(Constants.POOL_MAX_WAIT);

            this.jedisClusterClient = new JedisCluster(jedisNodes, connectionTimeout, socketTimeout, maxAttempts, poolConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Redis Cluster client: " + e.getMessage(), e);
        }
    }

    private void createStandaloneInstance(List<String> nodes) {
        if (nodes.size() != 1) {
            throw new IllegalArgumentException("Standalone instance requires exactly 1 node");
        }

        try {
            String[] hostPort = nodes.get(0).split(":");
            this.jedisPool = new JedisPool(new GenericObjectPoolConfig(), hostPort[0], Integer.parseInt(hostPort[1]));
            this.jedisClient = jedisPool.getResource();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Redis Standalone client: " + e.getMessage(), e);
        }
    }

    public Jedis getJedisClient() {
        if (redisServerMode != RedisServerMode.STANDALONE) {
            throw new IllegalStateException("JedisClient is available only in STANDALONE mode");
        }
        return jedisClient;
    }

    public JedisCluster getJedisClusterClient() {
        if (redisServerMode != RedisServerMode.CLUSTER) {
            throw new IllegalStateException("JedisClusterClient is available only in CLUSTER mode");
        }
        return jedisClusterClient;
    }

    public RedisServerMode getRedisServerMode(){
        return this.redisServerMode;
    }

    /**
     * Close the Redis connections.
     */
    public void close() {
        try {
            if (jedisClient != null) {
                jedisClient.close();
            }
            if (jedisPool != null) {
                jedisPool.close();
            }
            if (jedisClusterClient != null) {
                jedisClusterClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close Redis connections: " + e.getMessage(), e);
        }
    }

    /**
     * Builder class for RedisClient.
     */
    public static class RedisClientBuilder {
        private List<String> nodes;
        private Integer connectionTimeout = Constants.CONNECTION_TIMEOUT;
        private Integer socketTimeout = Constants.SOCKET_TIMEOUT;
        private RedisServerMode redisServerMode;
        private Integer maxAttempts = Constants.MAX_ATTEMPTS;

        public RedisClientBuilder(List<String> nodes) {
            this.nodes = nodes;
        }

        public RedisClientBuilder withServerMode(RedisServerMode redisServerMode) {
            this.redisServerMode = redisServerMode;
            return this;
        }

        public RedisClientBuilder withConnectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public RedisClientBuilder withSocketTimeout(Integer socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public RedisClientBuilder withMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public RedisClient build() {
            return new RedisClient(nodes, connectionTimeout, socketTimeout, redisServerMode, maxAttempts);
        }
    }
}
