package com.dylibso.examples.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

public class DateTest {
    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        System.out.println(m.writeValueAsString(new Order(ZonedDateTime.now(), 1., 2)));

    }
}
