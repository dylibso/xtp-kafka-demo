package com.dylibso.examples.kafka;

import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.io.InputStream;
import java.util.Map;

@RegisterRestClient(configKey = "extensions-api")
public interface XTPService {

    @GET
    @Path("/extension-points/{extension-point}/bindings/{guest-key}")
    @ClientHeaderParam(name = "Authorization", value = "Bearer ${xtp.token}")
    Map<String, Extension> fetch(
            @PathParam("extension-point") String extensionPoint,
            @PathParam("guest-key") String guestKey);

    @GET
    @Path("/c/{address}")
    @ClientHeaderParam(name = "Authorization", value = "Bearer ${xtp.token}")
    @Blocking
    InputStream fetchContent(@PathParam("address") String address);

    record Extension(String id, java.time.LocalDateTime updatedAt, String contentAddress){}
}
