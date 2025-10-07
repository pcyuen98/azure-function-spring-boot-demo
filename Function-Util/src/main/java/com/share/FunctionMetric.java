package com.share;

public enum FunctionMetric {
    FUNCTION_EXECUTION_COUNT("FunctionExecutionCount"),
    CPU_PERCENTAGE("MemoryWorkingSet");

    private final String metricName;

    FunctionMetric(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }
}
