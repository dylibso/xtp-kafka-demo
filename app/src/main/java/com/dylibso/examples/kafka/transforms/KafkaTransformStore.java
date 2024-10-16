package com.dylibso.examples.kafka.transforms;

import com.dylibso.examples.kafka.Header;
import com.dylibso.examples.kafka.Record;
import com.dylibso.examples.kafka.xtp.client.XTPService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
import org.extism.chicory.sdk.ExtismException;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Acts as a repository for the KafkaTransforms, and provides a unified
 * entry point to apply all the available transforms at once, and
 * return the results.
 */
public class KafkaTransformStore {
    private static final Logger LOGGER = Logger.getLogger(KafkaTransformStore.class);

    private final ConcurrentHashMap<String, KafkaTransform> transforms = new ConcurrentHashMap<>();

    /**
     * Applies the transform to the given record, serializing with the provided ObjectMapper.
     */
    public Multi<Record> transform(Record r, ObjectMapper mapper) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(r);
        return Multi.createFrom()
                .iterable(transforms.values())
                .flatMap(f -> {
                    try {
                        byte[] bs = f.transformBytes(bytes);
                        List<Header> headers = makeHeaders(f);
                        return Multi.createFrom().iterable(toRecords(mapper, bs, headers));
                    } catch (ExtismException ex) {
                        LOGGER.error("Caught error while processing", ex);
                        return Multi.createFrom().empty();
                    }
                });
    }

    /**
     * Create a list of headers from the metadata of the given transform.
     * <p>
     * Note: this could be cached instead of being recreated every time!
     */
    private List<Header> makeHeaders(KafkaTransform f) {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("plugin-name", f.name()));
        headers.add(new Header("plugin-timestamp", f.extension().updatedAt().toString()));
        return headers;
    }

    /**
     * Parses the given byte array to a list of Records and append the given additionalHeadrs to it, if any.
     */
    private List<Record> toRecords(ObjectMapper mapper, byte[] bs, List<Header> additionalHeaders) {
        try {
            List<Record> records = mapper.readValue(bs, new TypeReference<List<Record>>() {
            });
            if (records == null) {
                return List.of();
            }
            records.replaceAll(r -> r.withHeaders(additionalHeaders));
            return records;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Unconditionally register the given KafkaFilter, overwriting anything already registered.
     */
    public void register(KafkaTransform f) {
        transforms.put(f.name(), f);
        LOGGER.infof("Registered transform: '%s' with id '%s'",
                f.name(), f.extension().id());
    }

    /**
     * Update if the provided KafkaTransform is newer than the one in the store,
     * by checking its property {@link XTPService.Extension#updatedAt()}.
     */
    public void update(KafkaTransform kafkaTransform) {
        transforms.merge(kafkaTransform.name(), kafkaTransform, (existing, candidate) -> {
            LOGGER.infof("Updating transform: '%s' from id '%s' to id '%s'",
                    kafkaTransform.name(), existing.extension().id(), candidate.extension().id());
            if (!existing.extension().id().equals(candidate.extension().id()) ||
                    existing.extension().updatedAt().isBefore(candidate.extension().updatedAt())) {
                return candidate;
            } else {
                return existing;
            }
        });
    }

    /**
     * Remove the plugin with the given name from the store.
     */
    public void unregister(String name) {
        KafkaTransform removed = transforms.remove(name);
        LOGGER.infof("Unregistered transform: '%s' with id '%s'",
                removed.name(), removed.extension().id());
    }

    /**
     * Compare the given mapping Name -> Extension to the transforms in this store
     * and returns a "diff" mapping Name -> Status.
     */
    public Map<String, Status> compareStored(Map<String, XTPService.Extension> extensions) {
        var result = new HashMap<String, Status>();
        for (String name : extensions.keySet()) {
            result.put(name, Status.Updated);
        }
        for (var f : this.transforms.values()) {
            var name = f.name();
            if (extensions.containsKey(name)) {
                var candidate = extensions.get(name);
                KafkaTransform current = transforms.get(name);
                if (current == null || // no earlier binding
                        !current.extension().id().equals(candidate.id()) || // different extension for the same name binding
                        current.extension().updatedAt().isBefore(candidate.updatedAt()) // newer version of the previous extension
                ) {
                    result.put(name, Status.Updated);
                } else {
                    result.put(name, Status.Unchanged);
                }
            } else {
                result.put(name, Status.Deleted);
            }
        }
        return result;
    }

    public static enum Status {
        Unchanged, Updated, Deleted
    }

}
