package com.agenticsdd.knowledgeengine.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "lucenedb",
        mixinStandardHelpOptions = true,
        version = "lucenedb 0.1.0",
        description = "Lucene-backed code+doc index, search, and wiki generator — replaces qmd and graphify's retrieval CLI.",
        subcommands = {
                IndexCommand.class,
                SearchCommand.class,
                WikiCommand.class,
                DoctorCommand.class
        }
)
public final class LuceneDbCli implements Runnable {

    @Override
    public void run() {
        System.out.println("Run `lucenedb --help` for usage.");
    }

    public static void main(final String[] args) {
        final int exitCode = new CommandLine(new LuceneDbCli()).execute(args);
        System.exit(exitCode);
    }
}
