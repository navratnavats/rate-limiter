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

1. **RateLimiterFixedWindowInMemory**:
    - In-memory implementation for fixed window rate limiting.
    - Includes cleanup mechanisms for expired entries.

2. **RateLimiterFixedWindowWithRedis**:
    - Redis-based implementation for fixed window rate limiting.
    - Ensures atomic operations using Redis commands.

3. **RateLimiterSlidingWindow**:
    - Redis-based implementation for sliding window rate limiting.
    - Provides more accurate rate limiting compared to fixed window.

4. **RedisClient**:
    - Manages Redis connections (standalone or cluster).

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

## License

This library is licensed under the MIT License. See the `LICENSE` file for details.

