package com.example.poc.bus;

import java.util.function.Consumer;

public interface EventBus {
    void publish(String stream, String json);
    void consumeLoop(String stream, String group, String consumer, Consumer<String> handler);
}
