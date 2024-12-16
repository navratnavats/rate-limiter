package com.ratelimiter.common.constants;

public enum TimeUnit {
    MILLIS(1),
    SEC(1000),
    MIN(60000),
    HOURS(3600000),
    DAY(86400000);

    private Integer milliValue;

    private TimeUnit(Integer milliValue){
        this.milliValue = milliValue;
    }

    public Integer getMilliValue() {
        return milliValue;
    }
}
