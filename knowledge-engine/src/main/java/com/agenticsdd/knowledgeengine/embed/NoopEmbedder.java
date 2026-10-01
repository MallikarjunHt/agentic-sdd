package com.agenticsdd.knowledgeengine.embed;

/** Fallback embedder used when no model has been vendored yet. Vector search is disabled; BM25 still works. */
public final class NoopEmbedder implements Embedder {

    @Override
    public int dimensions() {
        return 0;
    }

    @Override
    public float[] embed(final String text) {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
