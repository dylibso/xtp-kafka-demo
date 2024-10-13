package com.dylibso.examples.kafka;

import java.time.ZonedDateTime;

public record Order(ZonedDateTime date, double price, long volume) { }
