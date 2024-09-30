package com.dylibso.examples.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import org.extism.chicory.sdk.Manifest;
import org.extism.chicory.sdk.ManifestWasm;
import org.extism.chicory.sdk.Plugin;

@ApplicationScoped
public class XTPProvider {
    @Produces
    @ApplicationScoped
    public KafkaFilter fetchFilter() {
        String greetWasm = "https://github.com/extism/plugins/releases/download/v1.1.0/greet.wasm";
        var manifest = Manifest.ofWasms(
                ManifestWasm.fromUrl(greetWasm).build()).build();
        return new KafkaFilter(Plugin.ofManifest(manifest).build());
    }

}
