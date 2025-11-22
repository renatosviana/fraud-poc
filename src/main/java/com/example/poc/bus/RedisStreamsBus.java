package com.example.poc.bus;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RedisStreamsBus implements EventBus {

    private final JedisPooled jedis;

    public RedisStreamsBus(String host, int port) {
        this.jedis = new JedisPooled(host, port);
    }

    @Override
    public void publish(String stream, String json) {
        // Jedis 5.x: xadd(key, id, map)
        jedis.xadd(stream, StreamEntryID.NEW_ENTRY, Map.of("data", json));
    }

    @Override
    public void consumeLoop(String stream, String group, String consumer, Consumer<String> handler) {
        // For the PoC we keep it simple: no consumer groups,
        // just read from the last ID we saw.
        String lastId = "0-0";

        while (true) {
            List<Map.Entry<String, List<StreamEntry>>> messages =
                    jedis.xread(
                            XReadParams.xReadParams()
                                    .block(2000)   // block up to 2s
                                    .count(64),    // max 64 entries per call
                            Map.of(stream, new StreamEntryID(lastId))
                    );

            if (messages == null) {
                continue; // timeout, try again
            }

            for (var streamResp : messages) {
                for (var entry : streamResp.getValue()) {
                    String json = entry.getFields().get("data");
                    if (json != null) {
                        handler.accept(json);
                    }
                    // advance lastId so we don't re-read the same entry
                    lastId = entry.getID().toString();
                }
            }
        }
    }
}
