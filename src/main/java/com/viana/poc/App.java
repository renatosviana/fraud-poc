package com.viana.poc;

import com.viana.poc.adapter.messaging.EventBus;
import com.viana.poc.adapter.messaging.RedisStreamsBus;
import com.viana.poc.adapter.persistence.AlertRepo;
import com.viana.poc.domain.service.Detector;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final Detector detector;
    private final AlertRepo repo;
    private final MeterRegistry metrics;

    @Value("${poc.redis.host}") String redisHost;
    @Value("${poc.redis.port}") int redisPort;
    @Value("${poc.streams.txStream}") String txStream;
    @Value("${poc.streams.alertStream}") String alertStream;
    @Value("${poc.group}") String group;
    @Value("${poc.consumer}") String consumer;

    public App(Detector detector, AlertRepo repo, MeterRegistry metrics){
        this.detector = detector; this.repo = repo; this.metrics = metrics;
    }

    public static void main(String[] args){ SpringApplication.run(App.class, args); }

    @Override
    public void run(String... args) {
        EventBus bus = new RedisStreamsBus(redisHost, redisPort);
        // consume txns -> detect -> save alerts
        new Thread(() -> bus.consumeLoop(txStream, group, consumer, json -> {
            try {
                var res = detector.evaluateJson(json);
                if (res.alert()) {
                    repo.save("unknownTxn", "unknownUser", res.reasons(), res.score());
                    bus.publish(alertStream, "{\"ok\":true}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }), "consumer").start();
        log.info("Detector started. Consuming from stream: " + txStream);
    }
}
