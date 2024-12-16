## **Rate Limiter Framework**

This project provides a modular and extensible rate-limiting solution for Java applications. It supports both **in-memory** and **Redis-based** implementations of the **Fixed Window Rate Limiting** algorithm. The `common` module provides shared utilities such as Redis client configuration, while the `fixedwindow` module implements the Fixed Window rate limiter logic.

---

### **Modules Overview**

#### **1. `common` Module**
- Provides utility classes for Redis connectivity and configuration.
- Supports both **Standalone Redis** and **Redis Cluster** modes using the `RedisClient` class.
- Implements the Builder pattern for flexible Redis configuration.

#### **2. `fixedwindow` Module**
- Implements Fixed Window Rate Limiting:
    - **In-Memory** implementation: Lightweight and suitable for single-instance applications.
    - **Redis-Based** implementation: Distributed and suitable for multi-instance environments.
- Uses Builder pattern for configuring the rate limiter.

---

### **Features**

#### **In-Memory Rate Limiter**
- Uses `ConcurrentHashMap` to store rate-limiting state.
- Cleans up stale data periodically using `ScheduledExecutorService`.
- Ideal for non-distributed systems or lightweight use cases.

#### **Redis-Based Rate Limiter**
- Leverages Redis for distributed state management.
- Supports both **Standalone** and **Clustered** Redis setups.
- Ensures atomic operations using Redis commands (`INCR` and `EXPIRE`).

---

### **Classes and Responsibilities**

#### **`common` Module**
1. **`RedisClient`**
    - Configures Redis connections.
    - Supports both Standalone and Cluster modes.
    - Implements a Builder pattern for flexibility.

   ```java
   RedisClient redisClient = new RedisClient.RedisClientBuilder(List.of("localhost:6379"))
           .withServerMode(RedisServerMode.STANDALONE)
           .build();
   ```

---

#### **`fixedwindow` Module**

##### **1. In-Memory Implementation**
- **`RateLimiterFixedWindowInMemory`**:
    - Manages the core Fixed Window logic in memory.
    - Cleans up expired entries using a background thread.
- **`RateLimiterFixedWindowInMemoryImpl`**:
    - Exposes the `isAllowed(String key)` method to check request limits.

  **Example**:
  ```java
  RateLimiterFixedWindowInMemory rateLimiter = new RateLimiterFixedWindowInMemory.Builder()
          .withLimit(5) // Maximum requests per window
          .withWindowSize(10000) // 10-second window
          .build();

  RateLimiterFixedWindowInMemoryImpl limiter = new RateLimiterFixedWindowInMemoryImpl(rateLimiter);
  boolean allowed = limiter.isAllowed("user123");
  ```

##### **2. Redis-Based Implementation**
- **`RateLimiterFixedWindowWithRedis`**:
    - Core Fixed Window logic using Redis for distributed state management.
    - Interacts with Redis using `INCR` and `EXPIRE`.
- **`RateLimiterFixedWindowWithRedisImpl`**:
    - Exposes the `isAllowed(String key)` method to check request limits.

  **Example**:
  ```java
  RedisClient redisClient = new RedisClient.RedisClientBuilder(List.of("localhost:6379"))
          .withServerMode(RedisServerMode.STANDALONE)
          .build();

  RateLimiterFixedWindowWithRedis rateLimiter = new RateLimiterFixedWindowWithRedis(redisClient, 60); // 60-second window

  RateLimiterFixedWindowWithRedisImpl limiter = new RateLimiterFixedWindowWithRedisImpl.Builder()
          .withFixedWindowWithRedis(rateLimiter)
          .withLimit(5) // Maximum requests per window
          .build();

  boolean allowed = limiter.isAllowed("user123");
  ```

---

### **How to Use**

#### **Step 1: Include Dependencies**
Add the required modules (`common` and `fixedwindow`) to your project. Ensure the necessary dependencies (`jedis`, `concurrent utilities`) are included in your `pom.xml`.

#### **Step 2: Configure Redis (if applicable)**
For Redis-based implementations, configure Redis settings (host, port, mode) using `RedisClient`.

#### **Step 3: Initialize the Rate Limiter**
Choose between **in-memory** and **Redis-based** implementations and configure the rate limiter using the Builder pattern.

---

### **Requirements**
- Java 8 or higher.
- Redis (for Redis-based rate limiter).
- Maven (to build the project).

---

### **Future Enhancements**
- Add support for Sliding Window and Token Bucket algorithms.
- Provide an external configuration file (e.g., YAML or properties) for dynamic rate limiter setup.

---
