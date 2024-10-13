package com.dylibso.examples.xtp.app.cli;

import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "bind")
public class BindFilters implements Runnable {

    @CommandLine.ParentCommand
    private EntryCommand parent;

    @CommandLine.Parameters(index = "0")
    File file;

    @CommandLine.Option(names={"--name", "-n"})
    String name;

    @Override
    public void run() {
        String name = this.name == null? file.getName() : this.name;
        var result = parent.xtpService.put(parent.extensionPoint, "plugin", parent.user, name, file);
        System.out.println(result);
    }
}
