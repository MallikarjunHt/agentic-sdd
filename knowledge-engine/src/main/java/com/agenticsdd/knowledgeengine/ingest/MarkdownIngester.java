package com.agenticsdd.knowledgeengine.ingest;

import com.agenticsdd.knowledgeengine.index.IndexedDocument;
import com.agenticsdd.knowledgeengine.index.LuceneIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Walks a directory for markdown/text docs and indexes each as a chunk, mirroring qmd's role —
 * without a Node.js runtime or network model download.
 */
public final class MarkdownIngester {

    private static final Logger LOG = LoggerFactory.getLogger(MarkdownIngester.class);
    private static final Set<String> EXCLUDED_DIR_NAMES =
            Set.of("node_modules", ".git", "dist", "build", "target", ".venv", "venv", "__pycache__");
    private static final Set<String> EXTENSIONS = Set.of(".md", ".txt");
    private static final int MAX_CHUNK_CHARS = 4000;

    private MarkdownIngester() {
    }

    /** Ingests every markdown/text file under {@code root}, skipping vendored/build directories. Returns count indexed. */
    public static int ingest(final Path root, final String sourceLabel, final LuceneIndex index) throws IOException {
        int count = 0;
        try (Stream<Path> paths = Files.walk(root)) {
            final List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> hasIndexableExtension(p))
                    .filter(p -> !isUnderExcludedDir(root, p))
                    .toList();
            for (final Path file : files) {
                final String text = Files.readString(file, StandardCharsets.UTF_8);
                for (final String chunk : chunk(text)) {
                    if (chunk.isBlank()) {
                        continue;
                    }
                    final String relPath = root.relativize(file).toString().replace('\\', '/');
                    final String id = sourceLabel + ":" + relPath + ":" + shortHash(chunk);
                    index.upsert(IndexedDocument.doc(id, relPath, sourceLabel, file.getFileName().toString(), chunk));
                    count++;
                }
            }
        }
        LOG.info("Ingested {} markdown/text chunks from {} ({})", count, root, sourceLabel);
        return count;
    }

    private static boolean hasIndexableExtension(final Path path) {
        final String name = path.getFileName().toString();
        return EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private static boolean isUnderExcludedDir(final Path root, final Path file) {
        final Path relative = root.relativize(file);
        for (int i = 0; i < relative.getNameCount(); i++) {
            if (EXCLUDED_DIR_NAMES.contains(relative.getName(i).toString())) {
                return true;
            }
        }
        return false;
    }

    /** Splits text into roughly-paragraph-sized chunks so embeddings and BM25 scoring stay focused. */
    private static List<String> chunk(final String text) {
        if (text.length() <= MAX_CHUNK_CHARS) {
            return List.of(text);
        }
        return List.of(text.split("\\n\\s*\\n")).stream()
                .filter(p -> !p.isBlank())
                .toList();
    }

    private static String shortHash(final String text) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
