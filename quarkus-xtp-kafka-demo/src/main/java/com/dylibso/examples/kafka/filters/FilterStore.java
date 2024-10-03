package com.dylibso.examples.kafka.filters;

import com.dylibso.examples.kafka.Header;
import com.dylibso.examples.kafka.Record;
import com.dylibso.examples.xtp.XTPService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FilterStore {
    private final ConcurrentHashMap<String, KafkaFilter> filters = new ConcurrentHashMap<>();

    /**
     * Applies the transform to the given record, serializing with the provided ObjectMapper.
     */
    public Multi<com.dylibso.examples.kafka.Record> transform(com.dylibso.examples.kafka.Record r, ObjectMapper mapper) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(r);
        return Multi.createFrom()
                .iterable(filters.values())
                .flatMap(f -> {
                    byte[] bs = f.transformBytes(bytes);
                    List<Header> headers = makeHeaders(f);
                    return Multi.createFrom().iterable(toRecords(mapper, bs, headers));
                });
    }

    private List<Header> makeHeaders(KafkaFilter f) {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("plugin-name", f.name().getBytes(StandardCharsets.UTF_8)));
        headers.add(new Header("plugin-id", f.extension().id().getBytes(StandardCharsets.UTF_8)));
        headers.add(new Header("plugin-timestamp", f.extension().updatedAt().toString().getBytes(StandardCharsets.UTF_8)));
        return headers;
    }

    /**
     * Unconditionally register the given KafkaFilter, overwriting anything already registered.
     */
    public void register(KafkaFilter f) {
        filters.put(f.extension().id(), f);
    }

    /**
     * Update if the provided KafkaFilter is newer than the one in the store,
     * by checking its property {@link XTPService.Extension#updatedAt()}.
     */
    public void update(KafkaFilter kafkaFilter) {
        filters.merge(kafkaFilter.extension().id(), kafkaFilter, (existing, candidate) ->
                existing.extension().updatedAt().isBefore(candidate.extension().updatedAt()) ?
                        candidate : existing);
    }

    public XTPService.Extension isNewer(XTPService.Extension candidate) {
        KafkaFilter current = filters.get(candidate.id());
        if (current == null) {
            return candidate;
        }
        if (current.extension().updatedAt().isBefore(candidate.updatedAt())) {
            return candidate;
        }
        return null;
    }

    private List<Record> toRecords(ObjectMapper mapper, byte[] bs, List<Header> headers) {
        try {
            List<Record> records = mapper.readValue(bs, new TypeReference<List<Record>>() {});
            records.replaceAll(r -> r.withHeaders(headers));
            return records;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
