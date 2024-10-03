package com.dylibso.examples.kafka;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.ArrayList;
import java.util.List;

public record Record(String topic, byte[] key, byte[] value, List<Header> headers) {
    public static Record of(KafkaRecord<byte[], byte[]> kr) {
        return new Record(kr.getTopic(), kr.getKey(), kr.getPayload(), extractHeaders(kr));
    }

    public Record withHeaders(List<Header> headers) {
        List<Header> h = this.headers == null ? new ArrayList<>() : new ArrayList<>(this.headers);
        h.addAll(headers);
        return new Record(topic, key, value, h);
    }

    private static List<Header> extractHeaders(KafkaRecord<byte[], byte[]> kr) {
        var l = new ArrayList<Header>();
        for (var h : kr.getHeaders()) {
            l.add(new Header(h.key(), h.value()));
        }
        return l;
    }

    public Message<byte[]> toOutgoingKafkaRecord() {
        RecordHeaders recordHeaders = recordHeaders();
        return new OutgoingKafkaRecord<>(
                topic, key, value, null, -1, recordHeaders, null, null, null);
    }

    private RecordHeaders recordHeaders() {
        RecordHeaders recordHeaders = new RecordHeaders();
        for (Header h : headers) {
            recordHeaders.add(new RecordHeader(h.key(), h.value()));
        }
        return recordHeaders;
    }
}

