package com.dylibso.examples.kafka;

import com.dylibso.examples.xtp.client.XTPService;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.extism.chicory.sdk.Manifest;
import org.extism.chicory.sdk.ManifestWasm;
import org.extism.chicory.sdk.Plugin;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusTest
public class XTPServiceTest {
    @RestClient
    XTPService svc;

    @ConfigProperty(name = "xtp.extension-point")
    String extensionPoint;

    @ConfigProperty(name = "xtp.guest-key")
    String guestKey;

    @Test
    public void testFetch() throws IOException {
        var extensions = svc.fetch(extensionPoint, guestKey);
        for (var ext : extensions.values()) {
            var dataStream = svc.fetchContent(ext.contentAddress());
            System.out.println(dataStream);
            Plugin plugin = Plugin.ofManifest(
                    Manifest.ofWasms(ManifestWasm.fromBytes(dataStream.readAllBytes()).build())
                            .build()).build();
        }
    }
}
