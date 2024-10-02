package com.dylibso.examples.kafka.filters;

import com.dylibso.examples.kafka.Record;
import com.dylibso.examples.xtp.XTPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.extism.chicory.sdk.Manifest;
import org.extism.chicory.sdk.ManifestWasm;
import org.extism.chicory.sdk.Plugin;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;

public class KafkaFilter {
    public static KafkaFilter fromInputStream(String pluginName, XTPService.Extension ext, InputStream is) throws IOException {
        ManifestWasm wasm = ManifestWasm.fromBytes(is.readAllBytes()).build();
        Plugin plugin = Plugin.ofManifest(Manifest.ofWasms(wasm).build()).build();
        return new KafkaFilter(plugin, pluginName, ext);
    }

    private static final Logger logger = Logger.getLogger(KafkaFilter.class);

    private final Plugin plugin;
    private final String pluginName;
    private final XTPService.Extension extension;

    public KafkaFilter(Plugin plugin, String pluginName, XTPService.Extension extension) {
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

    public com.dylibso.examples.kafka.Record transform(com.dylibso.examples.kafka.Record record, ObjectMapper mapper) throws IOException {
        return mapper.readValue(transformBytes(mapper.writeValueAsBytes(record)), Record.class);
    }

    public byte[] transformBytes(byte[] recordBytes) {
        logger.infof("transforming: %s", new String(recordBytes));
        byte[] result = plugin.call("transform", recordBytes);
        logger.infof("result: %s", new String(result));
        return result;
    }

}
