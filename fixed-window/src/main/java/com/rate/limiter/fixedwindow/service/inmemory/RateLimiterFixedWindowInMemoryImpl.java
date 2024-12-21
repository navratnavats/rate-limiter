package com.rate.limiter.fixedwindow.service.inmemory;

import com.rate.limiter.fixedwindow.configs.RateLimiterFixedWindowInMemory;

import java.util.Map;

public class RateLimiterFixedWindowInMemoryImpl {

    private final RateLimiterFixedWindowInMemory fixedWindowInMemory;

    public RateLimiterFixedWindowInMemoryImpl(RateLimiterFixedWindowInMemory fixedWindowInMemory) {
        this.fixedWindowInMemory = fixedWindowInMemory;
    }

    public boolean isAllowed(String key) {
        return fixedWindowInMemory.getRequestCounts().compute(key, (k, data) -> handleRequest(data)).getCount() <= fixedWindowInMemory.getLimit();
    }

    private RateLimiterFixedWindowInMemory.WindowData handleRequest(RateLimiterFixedWindowInMemory.WindowData data) {
        long currentTime = System.currentTimeMillis();
        long windowSizeMillis = fixedWindowInMemory.getWindowSizeMillis();

        if (data == null || currentTime - data.getStartTime() >= windowSizeMillis) {
            // Reset window
            return new RateLimiterFixedWindowInMemory.WindowData(1, currentTime);
        } else if (data.getCount() < fixedWindowInMemory.getLimit()) {
            // Increment count
            return new RateLimiterFixedWindowInMemory.WindowData(data.getCount() + 1, data.getStartTime());
        } else {
            // Return unchanged data (denied request)
            return data;
        }
    }
}
