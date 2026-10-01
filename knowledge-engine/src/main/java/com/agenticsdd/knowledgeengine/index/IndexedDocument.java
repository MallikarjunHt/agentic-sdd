package com.agenticsdd.knowledgeengine.index;

/**
 * A single unit of indexed content — either a markdown/doc chunk (qmd's former role) or a
 * graphify-extracted code symbol (graphify's former role). {@code id} must be stable across
 * re-indexing runs so {@link LuceneIndex#upsert} can replace rather than duplicate it.
 */
public record IndexedDocument(
        String id,
        String path,
        String type,
        String source,
        String title,
        String content,
        String community,
        Integer line
) {
    public static IndexedDocument doc(final String id, final String path, final String source,
                                       final String title, final String content) {
        return new IndexedDocument(id, path, Fields.TYPE_DOC, source, title, content, null, null);
    }

    public static IndexedDocument codeNode(final String id, final String path, final String source,
                                            final String title, final String content,
                                            final String community, final Integer line) {
        return new IndexedDocument(id, path, Fields.TYPE_CODE_NODE, source, title, content, community, line);
    }
}
