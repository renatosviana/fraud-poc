package com.viana.poc.domain.model;

import com.viana.poc.adapter.messaging.EventBus;
import com.viana.poc.adapter.messaging.RedisStreamsBus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viana.poc.model.Txn;

import java.util.Random;
import java.util.UUID;

public class TxnGenerator {
    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("REDIS_HOST","localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT","6379"));
        String stream = System.getenv().getOrDefault("TX_STREAM","txns");
        EventBus bus = new RedisStreamsBus(host, port);
        ObjectMapper om = new ObjectMapper();
        Random r = new Random();

        String[] users = {"u1","u2","u3","u4","u5"};
        String[] merchants = {"m1","m2","m3"};
        String[] countries = {"CA","US","BR","GB"};

        while (true) {
            String user = users[r.nextInt(users.length)];
            double base = switch (user) { case "u1" -> 40; case "u2" -> 120; default -> 80; };
            double amt = Math.max(1, base + r.nextGaussian()*30);
            // occasionally inject “fraud-like” spikes
            if (r.nextDouble()<0.05) amt = 1500 + r.nextDouble()*1000;
            var txn = new Txn(
                UUID.randomUUID().toString(), user, amt, "CAD",
                System.currentTimeMillis(),
                merchants[r.nextInt(merchants.length)],
                countries[r.nextInt(countries.length)],
                "dev-"+user
            );
            bus.publish(stream, om.writeValueAsString(txn));
            Thread.sleep(5); // ~200 tx/sec; tweak as needed
        }
    }
}
