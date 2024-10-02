package com.dylibso.examples.kafka.simulation;

import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class Simulator {
    private static final Logger LOGGER = Logger.getLogger(Simulator.class);

    @ConfigProperty(name = "simulator.enabled", defaultValue = "true")
    boolean enabled;

    @Channel("price-simulator")
    Emitter<byte[]> emitter;

    private BufferedReader reader;

    Simulator() throws IOException {
        this.reader = new BufferedReader(new FileReader("../EURUSD_1min.txt"));
        String heading = reader.readLine();// skip header.
        LOGGER.infof("heading: %s", heading);
    }

    @Scheduled(every = "1s")
    void produce() throws IOException {
        var line = reader.readLine();
        if (line == null) {
            LOGGER.info("EOF");
            return;
        }
        LOGGER.info(line);
        emitter.send(KafkaRecord.of("EURUSD".getBytes(StandardCharsets.UTF_8), line.getBytes(StandardCharsets.UTF_8)));
    }

}
