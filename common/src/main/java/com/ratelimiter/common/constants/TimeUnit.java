package com.ratelimiter.common.constants;

public enum TimeUnit {
    MILLIS(1),
    SECONDS(1000),
    MINUTES(60000),
    HOURS(3600000),
    DAYS(86400000);

    private final int milliValue;

    TimeUnit(int milliValue) {
        this.milliValue = milliValue;
    }

    public int getMilliValue() {
        return milliValue;
    }
}
