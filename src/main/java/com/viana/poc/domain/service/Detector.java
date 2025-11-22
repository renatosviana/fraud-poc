package com.viana.poc.domain.service;
import com.viana.poc.domain.model.Txn;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Detector {
    private final ObjectMapper om = new ObjectMapper();
    private final Map<String, RunningStats> userStats = new ConcurrentHashMap<>();
    private final Counter alertsTotal;
    private static final Logger log = LoggerFactory.getLogger(Detector.class);

    public Detector(MeterRegistry registry){
        this.alertsTotal = Counter.builder("alerts_total").register(registry);
    }

    public record Result(boolean alert, String reasons, double score){}

    public Result evaluateJson(String json) throws Exception {
        Txn t = om.readValue(json, Txn.class);
        RunningStats rs = userStats.computeIfAbsent(t.userId(), k -> new RunningStats());
        double z = rs.updateAndZ(t.amount());
        boolean ruleHighAmt = t.amount() > 1000.0;
        boolean ruleGeo = "BR".equalsIgnoreCase(t.country()) && t.amount() > 300.0; // example rule
        boolean anomaly = Math.abs(z) > 3.5;

        StringBuilder reasons = new StringBuilder();
        double score = 0.0;
        if (ruleHighAmt) { reasons.append("HIGH_AMOUNT;"); score += 0.6; }
        if (ruleGeo)     { reasons.append("GEO_RISK;"); score += 0.2; }
        if (anomaly)     { reasons.append("ANOMALY_ZSCORE;"); score += 0.4; }

        boolean alert = score >= 0.6 || reasons.length() > 0;
        if (alert) alertsTotal.increment();

        return new Result(alert, reasons.toString(), Math.min(1.0, score));
    }

    static class RunningStats {
        // simple rolling mean/std with exponential decay
        double mean=0, m2=0; int n=0;
        public double updateAndZ(double x){
            n++;
            double delta = x - mean;
            mean += delta / Math.min(n, 100); // crude decay
            double delta2 = x - mean;
            m2 += delta * delta2;
            double var = (n>1)? m2/Math.min(n-1, 100): 1e-6;
            double std = Math.sqrt(var) + 1e-6;
            return (x - mean)/std;
        }
    }
}
