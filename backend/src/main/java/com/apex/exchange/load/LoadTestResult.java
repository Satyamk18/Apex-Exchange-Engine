package com.apex.exchange.load;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadTestResult {
    private int totalOrders;
    private int threads;
    private double durationSeconds;
    private double throughputOpsPerSec;
    private double avgLatencyUs;
    private long timestamp;
}
