package com.agenticsdd.knowledgeengine.embed;

import java.nio.file.Path;

/** Resolves the configured embedding model directory to a working {@link Embedder}. */
public final class EmbedderFactory {

    /** Hidden-state size of all-MiniLM-L6-v2, the default vendored model. */
    public static final int EMBEDDING_DIMENSIONS = 384;

    private EmbedderFactory() {
    }

    public static Embedder create(final Path modelDir) {
        return OnnxEmbedder.load(modelDir, EMBEDDING_DIMENSIONS);
    }
}
