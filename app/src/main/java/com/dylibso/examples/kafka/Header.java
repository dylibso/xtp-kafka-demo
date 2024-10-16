package com.dylibso.examples.kafka;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Header(String key, String value) {}
