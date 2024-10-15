package com.dylibso.examples.kafka.transforms;

import com.dylibso.examples.kafka.Order;
import com.dylibso.examples.kafka.Record;
import com.dylibso.examples.kafka.xtp.client.XTPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTransformTest {

    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    public void testMaxTransform() throws IOException {
        var max = new File("../plugins/max/dist/plugin.wasm");
        var fis = new FileInputStream(max);
        var ext = new XTPService.Extension("max", LocalDateTime.now(), "xyz");
        var transform = KafkaTransform.fromInputStream("max", ext, fis);

        // A transform returns a list of values; in this case we know it's always one.
        List<Record> r1 = transform.transform(new Record("price-in", "EURUSD",
                new Order(ZonedDateTime.now(), 1.234, 10), List.of()), mapper);
        List<Record> r2 = transform.transform(new Record("price-in", "EURUSD",
                new Order(ZonedDateTime.now(), 1.200, 10), List.of()), mapper);

        // In both cases the result is the highest value (max).
        assertEquals(1.234, r1.getFirst().value().price());
        assertEquals(1.234, r2.getFirst().value().price());
    }

}