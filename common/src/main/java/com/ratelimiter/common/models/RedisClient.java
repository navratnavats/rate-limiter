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
    private final RedisServerMode redisServerMode;
    private final Integer connectionTimeout;
    private final Integer socketTimeout;
    private final Integer maxAttempts;
    private JedisPool jedisPool;
    private JedisCluster jedisClusterClient;

    private RedisClient(List<String> nodes, Integer connectionTimeout, Integer socketTimeout,
                        RedisServerMode redisServerMode, Integer maxAttempts) {
        if (redisServerMode == null) {
            throw new IllegalArgumentException("RedisServerMode must not be null");
        }

        this.nodes = nodes;
        this.redisServerMode = redisServerMode;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.maxAttempts = maxAttempts;

        if (redisServerMode == RedisServerMode.CLUSTER) {
            initializeCluster(nodes);
        } else if (redisServerMode == RedisServerMode.STANDALONE) {
            initializeStandalone(nodes);
        } else {
            throw new IllegalArgumentException("Unsupported RedisServerMode: " + redisServerMode);
        }
    }

    private void initializeCluster(List<String> nodes) {
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

    private void initializeStandalone(List<String> nodes) {
        if (nodes.size() != 1) {
            throw new IllegalArgumentException("Standalone instance requires exactly 1 node");
        }

        try {
            String[] hostPort = nodes.get(0).split(":");

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxWaitMillis(Constants.POOL_MAX_WAIT);
            poolConfig.setMaxTotal(30);

            this.jedisPool = new JedisPool(poolConfig, hostPort[0], Integer.parseInt(hostPort[1]));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Redis Standalone client: " + e.getMessage(), e);
        }
    }

    public JedisPool getJedisPool() {
        if (redisServerMode != RedisServerMode.STANDALONE) {
            throw new IllegalStateException("JedisPool is only available in standalone mode.");
        }
        return this.jedisPool;
    }

    public JedisCluster getJedisClusterClient() {
        if (redisServerMode != RedisServerMode.CLUSTER) {
            throw new IllegalStateException("JedisClusterClient is only available in cluster mode.");
        }
        return this.jedisClusterClient;
    }

    public RedisServerMode getRedisServerMode() {
        return this.redisServerMode;
    }

    public void close() {
        if (jedisPool != null) jedisPool.close();
        if (jedisClusterClient != null) jedisClusterClient.close();
    }

    public static class Builder {
        private List<String> nodes;
        private RedisServerMode redisServerMode;
        private Integer connectionTimeout = Constants.CONNECTION_TIMEOUT;
        private Integer socketTimeout = Constants.SOCKET_TIMEOUT;
        private Integer maxAttempts = Constants.MAX_ATTEMPTS;

        public Builder(List<String> nodes) {
            this.nodes = nodes;
        }

        public Builder withRedisServerMode(RedisServerMode redisServerMode) {
            this.redisServerMode = redisServerMode;
            return this;
        }

        public Builder withConnectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder withSocketTimeout(Integer socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder withMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public RedisClient build() {
            return new RedisClient(nodes, connectionTimeout, socketTimeout, redisServerMode, maxAttempts);
        }
    }
}
