package com.dylibso.examples.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ConcurrentHashMap;

public class FilterStore {
    private final ConcurrentHashMap<String, KafkaFilter> filters = new ConcurrentHashMap<>();

    /**
     * Applies the transform to the given record, serializing with the provided ObjectMapper.
     */
    public Multi<Record> transform(Record r, ObjectMapper mapper) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(r);
        return Multi.createFrom()
                .iterable(filters.values())
                .map(f -> f.transformBytes(bytes))
                .map(bs -> toRecord(mapper, bs));
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

    private Record toRecord(ObjectMapper mapper, byte[] bs) {
        try {
            return mapper.readValue(bs, Record.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
