package com.dylibso.examples.kafka.simulation;

import com.dylibso.examples.kafka.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.stream.DoubleStream;

public class Simulator {
    private static final Logger LOGGER = Logger.getLogger(Simulator.class);

    private final ObjectMapper mapper;
    private final Random random;
    private final double minPrice;
    private final double spanPrice;
    private final int maxVol;

    Simulator(double minPrice, double maxPrice, int maxVol, ObjectMapper mapper) throws IOException {
        this.mapper = mapper;
        this.random = new Random();
        this.minPrice = minPrice;
        this.spanPrice = maxPrice - minPrice;
        this.maxVol = maxVol;
    }

    byte[] next() throws IOException {
        var order = new Order(
                ZonedDateTime.now(),
                minPrice + random.nextGaussian(.5, .15) * spanPrice,
                1 + random.nextInt());
        var simulatedLine = mapper.writeValueAsBytes(order);
        return simulatedLine;
    }
}
