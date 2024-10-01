package com.dylibso.examples.kafka;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;

public record Record(String topic, byte[]key, byte[] payload) {
    static Record of(KafkaRecord<byte[], byte[]> kr) {
        return new Record(kr.getTopic(), kr.getKey(), kr.getPayload());
    }
}
