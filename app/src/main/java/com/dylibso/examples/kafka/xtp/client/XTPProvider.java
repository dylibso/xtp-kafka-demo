package com.dylibso.examples.kafka.xtp.client;

import com.dylibso.chicory.wasm.exceptions.UninstantiableException;
import com.dylibso.examples.kafka.transforms.KafkaTransformStore;
import com.dylibso.examples.kafka.transforms.KafkaTransformFetcher;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import org.jboss.logging.Logger;

import java.io.IOException;

/**
 * Fetches the initial data from the XTP service,
 * then periodically polls it for updates.
 */
@ApplicationScoped
public class XTPProvider {
    private static final Logger LOGGER = Logger.getLogger(XTPProvider.class);

    @Inject
    KafkaTransformFetcher fetcher;

    KafkaTransformStore kafkaTransformStore;

    @Produces
    @ApplicationScoped
    public KafkaTransformStore transformStore() {
        return kafkaTransformStore;
    }

    @Startup
    void onStart() throws IOException {
        LOGGER.info("The application is starting...");
        this.kafkaTransformStore = new KafkaTransformStore();
        var extensions = fetcher.extensions();
        for (var kv : extensions.entrySet()) {
            try {
                var transform = fetcher.fetchTransform(kv.getKey(), kv.getValue());
                kafkaTransformStore.register(transform);
            } catch (UninstantiableException ex) {
                LOGGER.error("Could not instantiate", ex);
            }
        }
    }

    @Scheduled(every = "1s")
    void checkUpdates() throws IOException {
        var extensions = fetcher.extensions();

        var results = kafkaTransformStore.compareStored(extensions);
        for (var kv : results.entrySet()) {
            final String name = kv.getKey();
            switch (kv.getValue()) {
                case Updated -> {
                    var transform = fetcher.fetchTransform(name, extensions.get(name));
                    kafkaTransformStore.update(transform);
                }
                case Deleted -> kafkaTransformStore.unregister(name);
                case Unchanged -> {}
            }
        }
    }

}
