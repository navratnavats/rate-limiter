package com.rate.limiter.fixedwindow.service.inmemory;

import com.rate.limiter.fixedwindow.configs.RateLimiterFixedWindowInMemory;

import java.util.Map;

public class RateLimiterFixedWindowInMemoryImpl {

    private final RateLimiterFixedWindowInMemory fixedWindowInMemory;

    public RateLimiterFixedWindowInMemoryImpl(RateLimiterFixedWindowInMemory fixedWindowInMemory) {
        this.fixedWindowInMemory = fixedWindowInMemory;
    }

    public boolean isAllowed(String key) {
        long currentTime = System.currentTimeMillis();
        long windowSizeMillis = fixedWindowInMemory.getWindowSizeMillis();
        int limit = fixedWindowInMemory.getLimit();

        Map<String, RateLimiterFixedWindowInMemory.WindowData> requestCounts = fixedWindowInMemory.getRequestCounts();

        RateLimiterFixedWindowInMemory.WindowData windowData = requestCounts.compute(key, (k, data) -> {
            if (data == null || currentTime - data.getStartTime() >= windowSizeMillis) {
                return new RateLimiterFixedWindowInMemory.WindowData(1, currentTime);
            } else if (data.getCount() < limit) {
                data.incrementCount();
                return data;
            } else {
                return data;
            }
        });

        return windowData != null && windowData.getCount() <= limit;
    }
}
