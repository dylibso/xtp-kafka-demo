package com.dylibso.examples.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    OutgoingKafkaRecord<byte[], byte[]>  read(KafkaRecord<byte[], byte[]> pricingData) throws IOException {
        var r = Record.of(pricingData);
        System.out.println("received-data:" + new String(pricingData.getPayload()));
        r = filters.transform(r, mapper);
        pricingData.ack();
        return KafkaRecord.of(r.key(), r.payload());
    }
}
