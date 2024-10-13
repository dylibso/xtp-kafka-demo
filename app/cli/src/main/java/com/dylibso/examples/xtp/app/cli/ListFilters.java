package com.dylibso.examples.xtp.app.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "list", aliases = {"ls"})
public class ListFilters implements Runnable {

    @CommandLine.ParentCommand
    private EntryCommand parent;

    @Override
    public void run() {
        var result = parent.xtpService.fetch(parent.extensionPoint, parent.guestKey);
        if (result.isEmpty()) {
            System.out.println("No transforms active at this time.");
        } else {
            for (var entries : result.entrySet()) {
                System.out.printf("%s: %s\n", entries.getKey(), entries.getValue().id());
            }
        }
    }
}
