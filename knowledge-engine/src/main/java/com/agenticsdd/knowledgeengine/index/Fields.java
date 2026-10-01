package com.agenticsdd.knowledgeengine.index;

/** Central registry of Lucene field names used across ingest, search, and wiki generation. */
public final class Fields {

    public static final String ID = "id";
    public static final String PATH = "path";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String TYPE = "type";
    public static final String SOURCE = "source";
    public static final String COMMUNITY = "community";
    public static final String LINE = "line";
    public static final String VECTOR = "vector";

    /** {@link #TYPE} value for a markdown/doc chunk (qmd's former role). */
    public static final String TYPE_DOC = "doc";

    /** {@link #TYPE} value for a graphify-extracted code symbol node. */
    public static final String TYPE_CODE_NODE = "code-node";

    private Fields() {
    }
}
