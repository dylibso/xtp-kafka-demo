package com.dylibso.examples.xtp.app.cli;

import com.dylibso.examples.xtp.client.XTPService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ServiceProvider {
    @RestClient
    XTPService xtpService;


    @Produces
    @ApplicationScoped
    public XTPService getService() {
        return xtpService;
    }
}
