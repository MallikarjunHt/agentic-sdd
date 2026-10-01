package com.agenticsdd.knowledgeengine.cli;

import com.agenticsdd.knowledgeengine.embed.Embedder;
import com.agenticsdd.knowledgeengine.embed.EmbedderFactory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "doctor", description = "Report index and embedding-model health.")
public final class DoctorCommand implements Callable<Integer> {

    @Option(names = "--index-dir", defaultValue = "./lucenedb-index", description = "Lucene index directory.")
    private Path indexDir;

    @Option(names = "--models-dir", defaultValue = "./models", description = "Directory containing model.onnx + vocab.txt.")
    private Path modelsDir;

    @Override
    public Integer call() {
        System.out.println("LuceneDb doctor");
        System.out.println("================");

        if (Files.exists(indexDir)) {
            try (var reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
                System.out.printf("Index: %s (%d documents)%n", indexDir, reader.numDocs());
            } catch (Exception e) {
                System.out.println("Index: " + indexDir + " — FAILED TO OPEN: " + e.getMessage());
            }
        } else {
            System.out.println("Index: none at " + indexDir + " — run `lucenedb index`");
        }

        final Embedder embedder = EmbedderFactory.create(modelsDir);
        if (embedder.isAvailable()) {
            System.out.printf("Embedding model: LOADED (dimensions=%d) — vector search enabled%n", embedder.dimensions());
        } else {
            System.out.println("Embedding model: NOT VENDORED at " + modelsDir + " — BM25 lexical search only.");
            System.out.println("  See " + modelsDir.resolve("README.md") + " for how to add it.");
        }
        return 0;
    }
}
