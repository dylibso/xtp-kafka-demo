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
        for (var ext : extensions.values()) {
            var filter = fetcher.fetchFilter(ext);
            filterStore.register(filter);
        }
    }

    @Scheduled(delay = 100, every = "30s")
    void checkUpdates() throws IOException {
        var extensions = fetcher.extensions();
        for (XTPService.Extension ext : extensions.values()) {
            var updated = filterStore.isNewer(ext);
            if (updated == null) {
                continue;
            }
            var filter = fetcher.fetchFilter(updated);
            filterStore.update(filter);
        }
    }

}
