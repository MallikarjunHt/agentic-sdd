package com.agenticsdd.knowledgeengine.cli;

import com.agenticsdd.knowledgeengine.embed.Embedder;
import com.agenticsdd.knowledgeengine.embed.EmbedderFactory;
import com.agenticsdd.knowledgeengine.index.LuceneIndex;
import com.agenticsdd.knowledgeengine.ingest.GraphifyIngester;
import com.agenticsdd.knowledgeengine.ingest.MarkdownIngester;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "index", description = "Index markdown docs and/or a graphify graph.json into the Lucene store.")
public final class IndexCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Directory to scan for markdown/text docs.")
    private Path sourceDir;

    @Option(names = "--source", required = true, description = "Label for this source, e.g. cpmp-auth.")
    private String source;

    @Option(names = "--index-dir", defaultValue = "./lucenedb-index", description = "Lucene index directory.")
    private Path indexDir;

    @Option(names = "--models-dir", defaultValue = "./models", description = "Directory containing model.onnx + vocab.txt.")
    private Path modelsDir;

    @Option(names = "--graph", description = "Path to a graphify graph.json to ingest alongside docs.")
    private Path graphJson;

    @Override
    public Integer call() throws IOException {
        if (!Files.isDirectory(sourceDir)) {
            System.err.println("Not a directory: " + sourceDir);
            return 1;
        }
        final Embedder embedder = EmbedderFactory.create(modelsDir);
        if (!embedder.isAvailable()) {
            System.out.println("[lucenedb] No embedding model vendored — indexing with BM25 only (see models/README.md).");
        }

        try (LuceneIndex index = new LuceneIndex(indexDir, embedder)) {
            final int docCount = MarkdownIngester.ingest(sourceDir, source, index);
            int codeCount = 0;
            if (graphJson != null) {
                if (!Files.exists(graphJson)) {
                    System.err.println("graphify graph.json not found: " + graphJson);
                    return 1;
                }
                codeCount = GraphifyIngester.ingest(graphJson, source, index);
            }
            index.commit();
            System.out.printf("[lucenedb] Indexed %d doc chunks and %d code nodes from '%s' into %s%n",
                    docCount, codeCount, source, indexDir);
        }
        return 0;
    }
}
