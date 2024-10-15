package com.dylibso.examples.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
public record Record(String topic, String key, Order value, List<Header> headers) {
    public static Record of(KafkaRecord<byte[], byte[]> kr, ObjectMapper mapper) throws IOException {
        return new Record(
                kr.getTopic(),
                new String(kr.getKey(), StandardCharsets.UTF_8),
                mapper.readValue(kr.getPayload(), Order.class),
                extractHeaders(kr));
    }

    public Record withHeaders(List<Header> headers) {
        List<Header> h = this.headers == null ? new ArrayList<>() : new ArrayList<>(this.headers);
        h.addAll(headers);
        return new Record(topic, key, value, h);
    }

    private static List<Header> extractHeaders(KafkaRecord<byte[], byte[]> kr) {
        var l = new ArrayList<Header>();
        for (var h : kr.getHeaders()) {
            l.add(new Header(h.key(), new String(h.value(), StandardCharsets.UTF_8)));
        }
        return l;
    }

    public Message<byte[]> toOutgoingKafkaRecord(ObjectMapper mapper) {
        RecordHeaders recordHeaders = recordHeaders();
        try {
            return new OutgoingKafkaRecord<>(
                    topic, key.getBytes(StandardCharsets.UTF_8), mapper.writeValueAsBytes(value), null, -1, recordHeaders, null, null, null);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private RecordHeaders recordHeaders() {
        RecordHeaders recordHeaders = new RecordHeaders();
        for (Header h : headers) {
            recordHeaders.add(new RecordHeader(h.key(), h.value().getBytes(StandardCharsets.UTF_8)));
        }
        return recordHeaders;
    }
}

