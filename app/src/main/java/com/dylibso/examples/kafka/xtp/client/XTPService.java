package com.dylibso.examples.kafka.xtp.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;
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

    @PUT
    @Path("/extension-points/{extension-point}/bindings/{guest-key}")
    @ClientHeaderParam(name = "Authorization", value = "Bearer ${xtp.token}")
    Map<String, Extension> putBindings(
            @PathParam("extension-point") String extensionPoint,
            Map<String, Extension> extensions);


    @GET
    @Path("/c/{address}")
    @ClientHeaderParam(name = "Authorization", value = "Bearer ${xtp.token}")
    InputStream fetchContent(@PathParam("address") String address);

    @PUT
    @Path("/artifacts/{extId}/{kind}/{owner}/{name}")
    @ClientHeaderParam(name = "Authorization", value = "Bearer ${xtp.token}")
    Artifact put(
            @PathParam("extId") String extId,
            @PathParam("kind") String kind,
            @PathParam("owner") String owner,
            @PathParam("name") String name,
            @RestForm("artifact") File file);

    record Extension(String id, java.time.LocalDateTime updatedAt, String contentAddress){}
    record Artifact(String id, String extensionPointId, String name, java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt){}
}
