package com.example.poc;

import com.example.poc.bus.EventBus;
import com.example.poc.bus.RedisStreamsBus;
import com.example.poc.detect.Detector;
import com.example.poc.store.AlertRepo;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== App started successfully ===");
        // TODO: keep the rest of the logic commented out for now
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    /*
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
        System.out.println("Detector started. Consuming from stream: " + txStream);
    }*/
}
