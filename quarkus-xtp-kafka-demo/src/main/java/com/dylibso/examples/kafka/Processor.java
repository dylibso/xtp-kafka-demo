package com.dylibso.examples.kafka;

import com.dylibso.examples.kafka.filters.FilterStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.io.IOException;

@ApplicationScoped
public class Processor {
    @Inject
    ObjectMapper mapper;

    @Inject
    FilterStore filters;

    @Channel("internal-price-broadcast")
    @Broadcast
    Emitter<Record> internalBroadcast;

    @Channel("internal-result-broadcast")
    @Broadcast
    Emitter<Record> internalResultBroadcast;


    @Incoming("pricing-data")
    @Outgoing("processed-price")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    Multi<Message<byte[]>> read(KafkaRecord<byte[], byte[]> pricingData) throws IOException {
        var r = Record.of(pricingData);
        internalBroadcast.send(r);
        return filters.transform(r, mapper)
                .invoke(m -> internalResultBroadcast.send(m))
                .map(Record::toOutgoingKafkaRecord)
                .onTermination().invoke(pricingData::ack);
    }
}
