package com.dylibso.examples.kafka.simulation;

import com.dylibso.examples.kafka.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;

public class FileSimulator {
    private static final Logger LOGGER = Logger.getLogger(FileSimulator.class);

    private BufferedReader reader;
    private final ObjectMapper mapper;

    FileSimulator(String fileName, ObjectMapper mapper) throws IOException {
        this.reader = new BufferedReader(new FileReader(fileName));
        this.mapper = mapper;
        String heading = reader.readLine();// skip header.
        LOGGER.debugf("heading: %s", heading);
    }

    byte[] next() throws IOException {
        var line = reader.readLine();
        if (line == null) {
            LOGGER.debugf("EOF");
            return null;
        }
        var data = line.split(",");
        var order = new Order(
                ZonedDateTime.now(),
                Double.parseDouble(data[4]),
                Long.parseLong(data[5]));
        var simulatedLine = mapper.writeValueAsBytes(order);
        LOGGER.info(new String(simulatedLine));
        return simulatedLine;
    }
}
