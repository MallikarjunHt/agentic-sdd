package com.agenticsdd.knowledgeengine.search;

/** One scored hit returned from a search — shape mirrors what wizard's retrieval/unified.py expects to format. */
public record SearchResult(
        String id,
        String path,
        String type,
        String source,
        String title,
        String snippet,
        String community,
        Integer line,
        float score
) {
}
