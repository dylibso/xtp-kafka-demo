package com.dylibso.examples.kafka;

import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class Processor {
    @Inject KafkaFilter filter;

    @Incoming("pricing-data")
    CompletionStage<Void> read(KafkaRecord<byte[], byte[]> pricingData) {
        System.out.println("received-data:" + new String(pricingData.getPayload()));
        pricingData.ack();
        return CompletableFuture.completedFuture(null);
    }
}
