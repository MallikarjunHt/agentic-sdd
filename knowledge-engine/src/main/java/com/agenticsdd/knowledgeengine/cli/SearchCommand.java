package com.agenticsdd.knowledgeengine.cli;

import com.agenticsdd.knowledgeengine.embed.EmbedderFactory;
import com.agenticsdd.knowledgeengine.search.LuceneSearcher;
import com.agenticsdd.knowledgeengine.search.SearchResult;
import org.apache.lucene.queryparser.classic.ParseException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "search", description = "Hybrid BM25 + vector search over the Lucene index. Prints raw results for a caller (e.g. wizard) to wrap and inject.")
public final class SearchCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Query text.")
    private String query;

    @Option(names = "--index-dir", defaultValue = "./lucenedb-index", description = "Lucene index directory.")
    private Path indexDir;

    @Option(names = "--models-dir", defaultValue = "./models", description = "Directory containing model.onnx + vocab.txt.")
    private Path modelsDir;

    @Option(names = "--limit", defaultValue = "10", description = "Max results.")
    private int limit;

    @Option(names = "--format", defaultValue = "context", description = "Output format: context (unwrapped result lines) or plain.")
    private String format;

    @Override
    public Integer call() {
        if (!Files.exists(indexDir)) {
            System.err.println("No index at " + indexDir + " — run `lucenedb index` first.");
            return 1;
        }
        try (LuceneSearcher searcher = new LuceneSearcher(indexDir, EmbedderFactory.create(modelsDir))) {
            final List<SearchResult> results = searcher.search(query, limit);
            if ("plain".equals(format)) {
                printPlain(results);
            } else {
                printContext(results);
            }
            return 0;
        } catch (IOException | ParseException e) {
            System.err.println("Search failed: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Prints raw result lines with NO wrapper tags — the caller (wizard's unified.py) owns
     * assembling the final &lt;knowledge_context source="..."&gt; block, so this must not
     * pre-wrap or the caller double-wraps it.
     */
    private void printContext(final List<SearchResult> results) {
        for (final SearchResult r : results) {
            final String loc = r.line() != null ? r.path() + ":" + r.line() : r.path();
            System.out.printf("[%s] %s (src=%s score=%.3f)%n  %s%n", r.type(), r.title(), loc, r.score(), r.snippet());
        }
    }

    private void printPlain(final List<SearchResult> results) {
        for (final SearchResult r : results) {
            System.out.printf("%.3f  [%s]  %s  (%s)%n", r.score(), r.type(), r.title(), r.path());
        }
    }
}
