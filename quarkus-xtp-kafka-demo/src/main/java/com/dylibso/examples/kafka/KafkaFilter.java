package com.dylibso.examples.kafka;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.extism.chicory.sdk.Plugin;

public class KafkaFilter {
    private final Plugin plugin;

    public KafkaFilter(Plugin plugin) {
        this.plugin = plugin;
    }

    public byte[] transform(KafkaRecord<byte[], byte[]> record) {
        return plugin.call("transform", record.getPayload());
    }

}
