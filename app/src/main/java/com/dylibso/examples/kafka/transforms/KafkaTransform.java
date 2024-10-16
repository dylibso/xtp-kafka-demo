package com.dylibso.examples.kafka.transforms;

import com.dylibso.examples.kafka.Header;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for a {@link Plugin} exposing a simple {@link #transform(Record, ObjectMapper)} method.
 */
public class KafkaTransform {

    public static KafkaTransform fromInputStream(
            String pluginName, XTPService.Extension ext, InputStream is) throws IOException {
        // Read the Wasm binary.
        var wasm = ManifestWasm.fromBytes(is.readAllBytes()).build();
        // Setup the instantiation option.
        var options = new Manifest.Options().withConfig(Map.of(
                        "transform-name", pluginName,
                        "topic-name", String.format("%s-output", pluginName)));
        // Create the Manifest.
        var manifest = Manifest.ofWasms(wasm).withOptions(options).build();
        // Instantiate the plug-in.
        var plugin = Plugin.ofManifest(manifest).build();
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

    /**
     * Applies the transform to a typed Record and returns the result as a List of Records.
     */
    public List<Record> transform(com.dylibso.examples.kafka.Record record, ObjectMapper mapper) throws IOException {
        return mapper.readValue(transformBytes(mapper.writeValueAsBytes(record)), new TypeReference<List<Record>>() {});
    }

    /**
     * Lower-level interface to the underlying plugin call.
     */
    public byte[] transformBytes(byte[] recordBytes) {
        logger.debugf("transforming: %s", new String(recordBytes));
        byte[] result = plugin.call("transform", recordBytes);
        logger.debugf("result: %s", new String(result));
        return result;
    }

    /**
     * Create a list of headers from the metadata of the given transform.
     *
     * Note: this could be cached instead of being recreated every time!
     */
    private List<Header> makeHeaders(String name, LocalDateTime updatedAt) {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("plugin-name", name));
        headers.add(new Header("plugin-timestamp", updatedAt.toString()));
        return headers;
    }


}
