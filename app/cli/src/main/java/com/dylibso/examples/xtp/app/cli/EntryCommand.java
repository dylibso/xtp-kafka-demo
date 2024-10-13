package com.dylibso.examples.xtp.app.cli;

import com.dylibso.examples.xtp.client.XTPService;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {ListFilters.class, BindFilters.class})
public class EntryCommand  {

    @Inject
    XTPService xtpService;

    @ConfigProperty(name = "xtp.guest-key")
    String guestKey;
    @ConfigProperty(name = "xtp.extension-point")
    String extensionPoint;
    @ConfigProperty(name = "xtp.user")
    String user;

}