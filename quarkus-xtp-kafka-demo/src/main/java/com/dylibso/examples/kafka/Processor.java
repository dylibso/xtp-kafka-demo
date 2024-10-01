package com.dylibso.examples.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.io.IOException;

@ApplicationScoped
public class Processor {
    @Inject
    ObjectMapper mapper;

    @Inject
    FilterStore filters;

    @Incoming("pricing-data")
    @Outgoing("processed-price")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    Multi<OutgoingKafkaRecord<byte[], byte[]>> read(KafkaRecord<byte[], byte[]> pricingData) throws IOException {
        var r = Record.of(pricingData);
        Multi<Record> transform = filters.transform(r, mapper);
        return transform.map(rec -> KafkaRecord.of(rec.topic(), rec.key(), rec.payload()));
    }
}
