package com.dylibso.examples.kafka;

import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Collection;

@ApplicationScoped
public class XTPProvider {
    private static final Logger LOGGER = Logger.getLogger("ListenerBean");
    @ConfigProperty(name = "xtp.guest-key")
    String guestKey;
    @ConfigProperty(name = "xtp.extension-point")
    String extensionPoint;

    @RestClient
    XTPService serviceClient;
    Logger logger = Logger.getLogger("xtp");

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
        var extensions = serviceClient.fetch(extensionPoint, guestKey);
        for (var ext : extensions.values()) {
            try (var dataStream = serviceClient.fetchContent(ext.contentAddress())) {
                filterStore.register(KafkaFilter.fromInputStream(ext, dataStream));
            }
        }
    }

    @Scheduled(delay = 100, every = "30s")
    void checkUpdates() throws IOException {
        var extensions = serviceClient.fetch(extensionPoint, guestKey);
        Collection<XTPService.Extension> newExtensions = filterStore.newer(extensions.values());
        for (var ext : newExtensions) {
            try (var dataStream = serviceClient.fetchContent(ext.contentAddress())) {
                filterStore.update(KafkaFilter.fromInputStream(ext, dataStream));
            }
        }


    }

}
