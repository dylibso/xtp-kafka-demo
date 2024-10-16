package com.dylibso.examples.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    @Test
    public void testTimestampFormat() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ZonedDateTime now = ZonedDateTime.of(2024, 10, 16, 11, 38, 00, 0, ZoneId.of("CET"));
        assertEquals(
                """
                {"date":"2024-10-16T11:38:00+02:00","price":1.0,"volume":2}
                """.trim(),
                m.writeValueAsString(new Order(now, 1., 2)));
    }
}
