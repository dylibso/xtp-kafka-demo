package com.dylibso.examples.kafka.simulation;

import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Simulator {
    private static final Logger LOGGER = Logger.getLogger(Simulator.class);

    private BufferedReader reader;

    Simulator(String fileName) throws IOException {
        this.reader = new BufferedReader(new FileReader(fileName));
        String heading = reader.readLine();// skip header.
        LOGGER.infof("heading: %s", heading);
    }

    byte[] next() throws IOException {
        var line = reader.readLine();
        if (line == null) {
            LOGGER.info("EOF");
            return null;
        }
        LOGGER.info(line);
        return line.getBytes(StandardCharsets.UTF_8);
    }


}
