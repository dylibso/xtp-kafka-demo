package com.dylibso.examples.kafka.simulation;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class SimulationProducer {
    private static final Logger LOGGER = Logger.getLogger(SimulationProducer.class);

    @Channel("price-simulator")
    Emitter<byte[]> emitter;

    Simulator simulator;

    @Inject SimulationSkipPredicated cfg;

    @Inject
    Vertx vertx;

    @Scheduled(every = "1s", skipExecutionIf = SimulationSkipPredicated.class)
    void produce() throws IOException {
        if (simulator == null) {
            this.simulator = new Simulator("../EURUSD_1min.txt");
        }
        byte[] data = this.simulator.next();
        if (data == null) {
            return;
        }
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
