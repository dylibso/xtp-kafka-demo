package com.dylibso.examples.kafka.transforms;

import com.dylibso.examples.kafka.Record;
import com.dylibso.examples.kafka.xtp.client.XTPService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.extism.chicory.sdk.Manifest;
import org.extism.chicory.sdk.ManifestWasm;
import org.extism.chicory.sdk.Plugin;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class KafkaTransform {
    public static KafkaTransform fromInputStream(String pluginName, XTPService.Extension ext, InputStream is) throws IOException {
        ManifestWasm wasm = ManifestWasm.fromBytes(is.readAllBytes()).build();
        Plugin plugin = Plugin.ofManifest(
                        Manifest.ofWasms(wasm)
                                .withOptions(new Manifest.Options()
                                        .withConfig(Map.of(
                                                "transform-name", pluginName,
                                                "topic-name", String.format("%s-output", pluginName)))).build())
                .build();
        return new KafkaTransform(plugin, pluginName, ext);
    }

    private static final Logger logger = Logger.getLogger(KafkaTransform.class);

    private final Plugin plugin;
    private final String pluginName;
    private final XTPService.Extension extension;

    public KafkaTransform(Plugin plugin, String pluginName, XTPService.Extension extension) {
        this.plugin = plugin;
        this.pluginName = pluginName;
        this.extension = extension;
    }

    public String name() {
        return pluginName;
    }

    public XTPService.Extension extension() {
        return extension;
    }

    public List<Record> transform(com.dylibso.examples.kafka.Record record, ObjectMapper mapper) throws IOException {
        return mapper.readValue(transformBytes(mapper.writeValueAsBytes(record)), new TypeReference<List<Record>>() {
        });
    }

    public byte[] transformBytes(byte[] recordBytes) {
        logger.infof("transforming: %s", new String(recordBytes));
        byte[] result = plugin.call("transform", recordBytes);
        logger.infof("result: %s", new String(result));
        return result;
    }

}
