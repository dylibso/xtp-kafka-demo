package com.dylibso.examples.kafka.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SimulationProducer {
    private static final Logger LOGGER = Logger.getLogger(SimulationProducer.class);

    @Inject
    ObjectMapper mapper;

    @Channel("price-simulator")
    Emitter<byte[]> emitter;

    Simulator simulator;

    @PostConstruct
    public void init() throws IOException {
        this.simulator = new Simulator(1.0468, 1.1244, 150, mapper);
    }

    @Scheduled(delay = 5, delayUnit = TimeUnit.SECONDS, every = "1s")
    void produce() throws IOException {
        byte[] data = this.simulator.next();
        emitter.send(KafkaRecord.of(
                "EURUSD".getBytes(StandardCharsets.UTF_8), data));
    }


    @ApplicationScoped
    static class SimulationSkipPredicated implements Scheduled.SkipPredicate {
        @ConfigProperty(name = "simulator.enabled", defaultValue = "true")
        boolean enabled;

        @Override
        public boolean test(ScheduledExecution execution) {
            return !enabled;
        }
    }
}
