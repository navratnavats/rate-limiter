# Rate Limiter Library

This library provides implementations for rate limiting using both **fixed window** and **sliding window** algorithms. It supports **in-memory** and **Redis-based** backends, making it suitable for distributed systems or single-node applications. The library is modular, thread-safe, and customizable.

---

## Features

- **Fixed Window Rate Limiting**:
    - In-memory implementation for local applications.
    - Redis-based implementation for distributed systems.
- **Sliding Window Rate Limiting**:
    - Redis-based implementation for precise rate limiting.
- Supports both **standalone** and **clustered** Redis configurations.
- Thread-safe and scalable design.
- Cleanup mechanisms for expired keys in in-memory implementations.

---

## Installation

Include the JAR file in your project:

```xml
<dependency>
    <groupId>com.ratelimiter</groupId>
    <artifactId>rate-limiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Usage

### 1. **Fixed Window Rate Limiter (In-Memory)**

#### Example:
```java
RateLimiterFixedWindowInMemory rateLimiter = new RateLimiterFixedWindowInMemory.Builder()
    .withLimit(5) // Maximum 5 requests
    .withWindowSize(10000) // Window size: 10 seconds
    .build();

RateLimiterFixedWindowInMemoryImpl rateLimiterImpl = new RateLimiterFixedWindowInMemoryImpl(rateLimiter);

String clientKey = "client1";
for (int i = 0; i < 7; i++) {
    boolean allowed = rateLimiterImpl.isAllowed(clientKey);
    System.out.println("Request " + (i + 1) + ": " + (allowed ? "Allowed" : "Denied"));
}

rateLimiter.shutdown();
```

#### Output:
```text
Request 1: Allowed
Request 2: Allowed
Request 3: Allowed
Request 4: Allowed
Request 5: Allowed
Request 6: Denied
Request 7: Denied
```

---

### 2. **Fixed Window Rate Limiter (Redis)**

#### Example:
```java
RedisClient redisClient = new RedisClient.Builder(List.of("localhost:6379"))
    .withRedisServerMode(RedisServerMode.STANDALONE)
    .build();

RateLimiterFixedWindowWithRedis fixedWindowWithRedis = new RateLimiterFixedWindowWithRedis(
    redisClient,
    60, // Window size in seconds
    5   // Allow 5 requests per window
);

RateLimiterFixedWindowWithRedisImpl rateLimiter = RateLimiterFixedWindowWithRedisImpl.builder()
    .withFixedWindowWithRedis(fixedWindowWithRedis)
    .build();

String clientKey = "client1";
for (int i = 0; i < 7; i++) {
    boolean allowed = rateLimiter.isAllowed(clientKey);
    System.out.println("Request " + (i + 1) + ": " + (allowed ? "Allowed" : "Denied"));
}
```

#### Output:
```text
Request 1: Allowed
Request 2: Allowed
Request 3: Allowed
Request 4: Allowed
Request 5: Allowed
Request 6: Denied
Request 7: Denied
```

---

### 3. **Sliding Window Rate Limiter (Redis)**

#### Example:
```java
RedisClient redisClient = new RedisClient.Builder(List.of("localhost:6379"))
    .withRedisServerMode(RedisServerMode.STANDALONE)
    .build();

RateLimiterSlidingWindow rateLimiterConfig = new RateLimiterSlidingWindow
    .RateLimiterSlidingWindowBuilder()
    .redisClient(redisClient)
    .timeUnit(TimeUnit.SECONDS)
    .windowSize(10) // 10 seconds sliding window
    .limit(5)       // Allow 5 requests in the window
    .build();

RateLimiterSlidingWindowImpl slidingWindowRateLimiter = new RateLimiterSlidingWindowImpl(rateLimiterConfig);

String clientKey = "client1";
for (int i = 0; i < 7; i++) {
    boolean allowed = slidingWindowRateLimiter.isAllowed(rateLimiterConfig, clientKey);
    System.out.println("Request " + (i + 1) + ": " + (allowed ? "Allowed" : "Denied"));
}
```

#### Output:
```text
Request 1: Allowed
Request 2: Allowed
Request 3: Allowed
Request 4: Allowed
Request 5: Allowed
Request 6: Denied
Request 7: Denied
```

---

## Configuration Classes

### RedisClient
- Supports standalone and cluster Redis configurations.
- Builder pattern for flexible initialization.

#### Example:
```java
RedisClient redisClient = new RedisClient.Builder(List.of("localhost:6379"))
    .withRedisServerMode(RedisServerMode.CLUSTER)
    .withConnectionTimeout(1000)
    .withSocketTimeout(1000)
    .build();
```

### TimeUnit
- Enum to represent time units like milliseconds, seconds, and minutes.

---

## Key Classes and Responsibilities

### 1. **RateLimiterFixedWindowInMemory**
- **Purpose**: Implements fixed window rate limiting using in-memory storage.
- **Responsibilities**:
    - Stores request counts for each key in a fixed time window.
    - Periodically cleans up expired entries to free up memory.
- **Use Case**: Ideal for single-node applications where a distributed backend is not required.

### 2. **RateLimiterFixedWindowWithRedis**
- **Purpose**: Implements fixed window rate limiting using Redis for distributed systems.
- **Responsibilities**:
    - Utilizes Redis atomic commands like `INCR` and `EXPIRE` to manage request counts.
    - Supports both standalone and clustered Redis setups.
- **Use Case**: Suitable for distributed systems where rate limiting consistency across nodes is essential.

### 3. **RateLimiterSlidingWindow**
- **Purpose**: Implements sliding window rate limiting using Redis for precise control.
- **Responsibilities**:
    - Maintains request timestamps in a Redis sorted set for each key.
    - Dynamically removes expired timestamps to enforce sliding window limits.
- **Use Case**: Best for scenarios requiring more accurate rate limiting than fixed window.

### 4. **RedisClient**
- **Purpose**: Manages connections to Redis, supporting both standalone and cluster modes.
- **Responsibilities**:
    - Provides connection pooling and configuration management.
    - Abstracts Redis operations for use in rate limiting.
- **Use Case**: Utility class used internally by Redis-based rate limiters.

### 5. **TimeUnit**
- **Purpose**: Enum for representing time units (e.g., milliseconds, seconds).
- **Responsibilities**:
    - Converts time units to milliseconds for consistent calculations.
- **Use Case**: Used in configuration classes to specify window durations.

---

## Building and Packaging

To build the library and create a JAR file:

1. Navigate to the project directory.
2. Run the following command:

```bash
mvn clean package
```

The JAR file will be available in the `target` directory.

---

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

---


### **Future Enhancements**
- Add support for Token Bucket algorithms.
- Provide an external configuration file (e.g., YAML or properties) for dynamic rate limiter setup.

---