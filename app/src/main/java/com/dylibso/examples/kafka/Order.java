package com.dylibso.examples.kafka;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.ZonedDateTime;

@RegisterForReflection
public record Order(ZonedDateTime date, double price, long volume) { }
