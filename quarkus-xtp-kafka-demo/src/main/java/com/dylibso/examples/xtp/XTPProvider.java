package com.dylibso.examples.xtp;

import com.dylibso.examples.kafka.filters.FilterStore;
import com.dylibso.examples.kafka.filters.KafkaFilterFetcher;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class XTPProvider {
    private static final Logger LOGGER = Logger.getLogger(XTPProvider.class);

    @Inject
    KafkaFilterFetcher fetcher;

    FilterStore filterStore;

    @Produces
    @ApplicationScoped
    public FilterStore filterChain() {
        return filterStore;
    }

    @Startup
    void onStart() throws IOException {
        LOGGER.info("The application is starting...");
        this.filterStore = new FilterStore();
        var extensions = fetcher.extensions();
        for (var kv : extensions.entrySet()) {
            var filter = fetcher.fetchFilter(kv.getKey(), kv.getValue());
            filterStore.register(filter);
        }
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkUpdates();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

//    @Scheduled(delay = 100, every = "10s")
    void checkUpdates() throws IOException {
        var extensions = fetcher.extensions();

        var results = filterStore.compareStored(extensions);
        LOGGER.infof("update: %s", results);
        for (var kv : results.entrySet()) {
            final String name = kv.getKey();
            switch (kv.getValue()) {
                case Updated -> {
                    var filter = fetcher.fetchFilter(name, extensions.get(name));
                    filterStore.update(filter);
                }
                case Deleted -> filterStore.unregister(name);
                case Unchanged -> {}
            }
        }
    }

}
