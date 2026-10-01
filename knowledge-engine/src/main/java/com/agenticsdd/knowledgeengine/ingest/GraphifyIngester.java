package com.agenticsdd.knowledgeengine.ingest;

import com.agenticsdd.knowledgeengine.index.IndexedDocument;
import com.agenticsdd.knowledgeengine.index.LuceneIndex;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Ingests a graphify {@code graph.json} (AST-extracted code symbols + community detection) into
 * Lucene, so a single index/search covers both code structure and docs — replacing graphify's
 * separate Python-CLI-backed retrieval path with the same store used for markdown.
 *
 * <p>Reads graphify's own output rather than re-implementing AST extraction: graphify still
 * produces {@code graph.json}, this class just ingests it.
 */
public final class GraphifyIngester {

    private static final Logger LOG = LoggerFactory.getLogger(GraphifyIngester.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GraphifyIngester() {
    }

    /** Ingests every node in a graphify graph.json as a code-node document. Returns count indexed. */
    public static int ingest(final Path graphJsonPath, final String sourceLabel, final LuceneIndex index) throws IOException {
        final JsonNode root = MAPPER.readTree(graphJsonPath.toFile());
        final JsonNode nodes = root.path("nodes");
        int count = 0;
        for (final JsonNode node : nodes) {
            final String id = node.path("id").asText(null);
            if (id == null) {
                continue;
            }
            final String label = node.path("label").asText(node.path("norm_label").asText(""));
            final String sourceFile = node.path("source_file").asText("");
            final String location = node.path("source_location").asText("");
            final String community = node.has("community") ? node.path("community").asText() : null;
            final Integer line = parseLine(location);

            final String content = label + " " + sourceFile + " " + location;
            final String docId = sourceLabel + ":" + id;

            index.upsert(IndexedDocument.codeNode(docId, sourceFile, sourceLabel, label, content, community, line));
            count++;
        }
        LOG.info("Ingested {} code-graph nodes from {} ({})", count, graphJsonPath, sourceLabel);
        return count;
    }

    /** graphify encodes location as e.g. "L42" — parse the leading line number, or null if absent. */
    private static Integer parseLine(final String location) {
        if (location == null || !location.startsWith("L")) {
            return null;
        }
        try {
            return Integer.parseInt(location.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
