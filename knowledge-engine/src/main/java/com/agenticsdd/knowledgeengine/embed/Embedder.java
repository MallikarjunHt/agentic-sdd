package com.agenticsdd.knowledgeengine.embed;

/** Produces a fixed-length float vector embedding for a piece of text. */
public interface Embedder {

    /** Dimensionality of vectors this embedder produces. */
    int dimensions();

    /** Embed a single piece of text. Returns null if embedding is unavailable (model not vendored). */
    float[] embed(String text);

    /** True if this embedder can actually produce vectors (model file present and loaded). */
    boolean isAvailable();
}
