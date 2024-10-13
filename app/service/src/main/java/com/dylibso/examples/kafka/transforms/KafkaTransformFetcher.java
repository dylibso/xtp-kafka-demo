package com.dylibso.examples.kafka.transforms;

import com.dylibso.examples.xtp.client.XTPService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@ApplicationScoped
public class KafkaTransformFetcher {
    @RestClient
    XTPService xtpService;
    @ConfigProperty(name = "xtp.guest-key")
    String guestKey;
    @ConfigProperty(name = "xtp.extension-point")
    String extensionPoint;

    public Map<String, XTPService.Extension> extensions() {
        return this.xtpService.fetch(extensionPoint, guestKey);
    }

    public KafkaTransform fetchTransform(String pluginName, XTPService.Extension ext) throws IOException {
        try (InputStream is = this.xtpService.fetchContent(ext.contentAddress())) {
            return KafkaTransform.fromInputStream(pluginName, ext, is);
        }
    }
}
