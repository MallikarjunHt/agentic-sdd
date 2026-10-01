package com.agenticsdd.knowledgeengine.wiki;

import com.agenticsdd.knowledgeengine.index.Fields;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates a browsable static wiki (markdown pages, cross-linked) from the Lucene index —
 * one page per community (graphify's clustering) plus one per doc source, so the index is
 * inspectable outside of Claude/wizard, similar in spirit to graphify's GRAPH_REPORT.md.
 */
public final class WikiGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(WikiGenerator.class);

    private WikiGenerator() {
    }

    public static void generate(final Path indexDir, final Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            final Map<String, List<PageEntry>> byCommunity = new TreeMap<>();
            final Map<String, List<PageEntry>> bySource = new TreeMap<>();

            for (final LeafReaderContext leaf : reader.leaves()) {
                final StoredFields storedFields = leaf.reader().storedFields();
                for (int docId = 0; docId < leaf.reader().maxDoc(); docId++) {
                    final Document doc = storedFields.document(docId);
                    final PageEntry entry = toEntry(doc);
                    if (entry == null) {
                        continue;
                    }
                    final String community = doc.get(Fields.COMMUNITY);
                    if (community != null) {
                        byCommunity.computeIfAbsent(community, k -> new ArrayList<>()).add(entry);
                    }
                    bySource.computeIfAbsent(entry.source, k -> new ArrayList<>()).add(entry);
                }
            }

            writeIndexPage(outputDir, byCommunity, bySource);
            for (final Map.Entry<String, List<PageEntry>> e : byCommunity.entrySet()) {
                writeCommunityPage(outputDir, e.getKey(), e.getValue());
            }
            for (final Map.Entry<String, List<PageEntry>> e : bySource.entrySet()) {
                writeSourcePage(outputDir, e.getKey(), e.getValue());
            }
            LOG.info("Generated wiki at {} — {} communities, {} sources", outputDir, byCommunity.size(), bySource.size());
        }
    }

    private static PageEntry toEntry(final Document doc) {
        final String id = doc.get(Fields.ID);
        if (id == null) {
            return null;
        }
        return new PageEntry(id, doc.get(Fields.TITLE), doc.get(Fields.PATH),
                doc.get(Fields.TYPE), doc.get(Fields.SOURCE), doc.get(Fields.LINE));
    }

    private static void writeIndexPage(final Path outputDir, final Map<String, List<PageEntry>> byCommunity,
                                        final Map<String, List<PageEntry>> bySource) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append("# LuceneDb Wiki\n\n");
        sb.append("Auto-generated index. Regenerate with `lucenedb wiki`.\n\n");
        sb.append("## Sources\n\n");
        for (final String source : bySource.keySet()) {
            sb.append("- [").append(source).append("](source-").append(slug(source)).append(".md) — ")
                    .append(bySource.get(source).size()).append(" documents\n");
        }
        sb.append("\n## Communities (code clusters)\n\n");
        for (final String community : byCommunity.keySet()) {
            sb.append("- [Community ").append(community).append("](community-").append(slug(community)).append(".md) — ")
                    .append(byCommunity.get(community).size()).append(" symbols\n");
        }
        Files.writeString(outputDir.resolve("index.md"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static void writeCommunityPage(final Path outputDir, final String community,
                                            final List<PageEntry> entries) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append("# Community ").append(community).append("\n\n");
        sb.append("[← Index](index.md)\n\n");
        entries.stream()
                .sorted(Comparator.comparing(e -> e.path == null ? "" : e.path))
                .forEach(e -> sb.append("- **").append(e.title).append("** — `")
                        .append(e.path).append(e.line != null ? ":" + e.line : "").append("`\n"));
        Files.writeString(outputDir.resolve("community-" + slug(community) + ".md"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static void writeSourcePage(final Path outputDir, final String source,
                                         final List<PageEntry> entries) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append("# Source: ").append(source).append("\n\n");
        sb.append("[← Index](index.md)\n\n");
        entries.stream()
                .sorted(Comparator.comparing(e -> e.path == null ? "" : e.path))
                .forEach(e -> sb.append("- **").append(e.title).append("** (").append(e.type).append(") — `")
                        .append(e.path).append(e.line != null ? ":" + e.line : "").append("`\n"));
        Files.writeString(outputDir.resolve("source-" + slug(source) + ".md"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String slug(final String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    private record PageEntry(String id, String title, String path, String type, String source, String line) {
    }
}
