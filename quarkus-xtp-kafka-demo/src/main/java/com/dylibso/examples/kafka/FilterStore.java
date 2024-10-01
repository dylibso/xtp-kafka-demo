package com.dylibso.examples.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilterStore {
    private final ConcurrentHashMap<String, KafkaFilter> filters = new ConcurrentHashMap<>();

    public void update(KafkaFilter f) {
        // Update the filter with the incoming filter only if not present, and if it is newer than the filter that we have.
        filters.merge(f.extension().id(), f, (current, incoming) ->
                current.extension().updatedAt().isAfter(incoming.extension().updatedAt()) ? current : incoming);
        filters.put(f.extension().id(), f);
    }

    public Collection<Record> transform(Record r, ObjectMapper mapper) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(r);
        return filters.values().stream().parallel()
                .map(f -> f.transformBytes(bytes)).map(bs -> toRecord(mapper, bs)).toList();
    }

    private Record toRecord(ObjectMapper mapper, byte[] bs){
        try {
            return mapper.readValue(bs, Record.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
