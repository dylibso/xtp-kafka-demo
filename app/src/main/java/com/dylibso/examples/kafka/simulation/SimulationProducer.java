package com.dylibso.examples.kafka.simulation;

import com.dylibso.examples.kafka.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * The SimulationProducer publishes data on a price-simulator channel,
 * which writes to a Kafka topic "pricing-data".
 * <p>
 * The SimulationProducer can be disabled: in that case, the application
 * will continue to read data from "pricing-data".
 */
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
        this.simulator = new Simulator(1.0468, 1.1244, 150);
    }

    @Scheduled(delay = 5, delayUnit = TimeUnit.SECONDS, every = "1s", skipExecutionIf = SimulatorNotEnabled.class)
    void produce() throws IOException {
        Order order = this.simulator.next();
        byte[] data = mapper.writeValueAsBytes(order);
        emitter.send(KafkaRecord.of(
                "EURUSD".getBytes(StandardCharsets.UTF_8), data));
    }


    @ApplicationScoped
    static class SimulatorNotEnabled implements Scheduled.SkipPredicate {
        @ConfigProperty(name = "simulator.enabled", defaultValue = "true")
        boolean enabled;

        @Override
        public boolean test(ScheduledExecution execution) {
            return !enabled;
        }
    }
}
