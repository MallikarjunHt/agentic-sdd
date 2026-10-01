package com.agenticsdd.knowledgeengine.cli;

import com.agenticsdd.knowledgeengine.wiki.WikiGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "wiki", description = "Generate browsable markdown wiki pages from the Lucene index.")
public final class WikiCommand implements Callable<Integer> {

    @Option(names = "--index-dir", defaultValue = "./lucenedb-index", description = "Lucene index directory.")
    private Path indexDir;

    @Option(names = "--out", defaultValue = "./lucenedb-wiki", description = "Output directory for generated wiki pages.")
    private Path outputDir;

    @Override
    public Integer call() throws IOException {
        if (!Files.exists(indexDir)) {
            System.err.println("No index at " + indexDir + " — run `lucenedb index` first.");
            return 1;
        }
        WikiGenerator.generate(indexDir, outputDir);
        System.out.println("[lucenedb] Wiki generated at " + outputDir);
        return 0;
    }
}
