package com.rate.limiter.fixedwindow.configs;

import java.util.Map;
import java.util.concurrent.*;

public class RateLimiterFixedWindowInMemory {

    private final int limit;
    private final long windowSizeMillis;
    private final Map<String, WindowData> requestCounts;
    private final ScheduledExecutorService executorService;

    public RateLimiterFixedWindowInMemory(Builder builder) {
        this.limit = builder.limit;
        this.windowSizeMillis = builder.windowSizeMillis;
        this.executorService = builder.executorService != null
                ? builder.executorService
                : Executors.newScheduledThreadPool(1);
        this.requestCounts = new ConcurrentHashMap<>();

        // Start a periodic cleanup task
        this.executorService.scheduleAtFixedRate(this::cleanupExpiredEntries, windowSizeMillis, windowSizeMillis, TimeUnit.MILLISECONDS);
    }

    public Map<String, WindowData> getRequestCounts() {
        return requestCounts;
    }

    public int getLimit() {
        return limit;
    }

    public long getWindowSizeMillis() {
        return windowSizeMillis;
    }

    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        requestCounts.entrySet().removeIf(entry -> currentTime - entry.getValue().startTime >= windowSizeMillis);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Builder for FixedWindowRateLimiter.
     */
    public static class Builder {
        private int limit; // Default limit
        private long windowSizeMillis; // Default window size (60 seconds)
        private ScheduledExecutorService executorService;

        public Builder withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder withWindowSize(long windowSizeMillis) {
            this.windowSizeMillis = windowSizeMillis;
            return this;
        }

        public Builder withExecutorService(ScheduledExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public RateLimiterFixedWindowInMemory build() {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be greater than 0");
            }
            if (windowSizeMillis <= 0) {
                throw new IllegalArgumentException("Window size must be greater than 0");
            }
            return new RateLimiterFixedWindowInMemory(this);
        }
    }

    /**
     * Inner class to hold window data.
     */
    public static class WindowData {
        private int count;
        private long startTime;

        public WindowData(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }

        public int getCount() {
            return count;
        }

        public void incrementCount() {
            this.count++;
        }

        public long getStartTime() {
            return startTime;
        }

        public void resetWindow(long newStartTime) {
            this.count = 1;
            this.startTime = newStartTime;
        }
    }
}
