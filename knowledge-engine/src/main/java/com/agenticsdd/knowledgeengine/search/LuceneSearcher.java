package com.agenticsdd.knowledgeengine.search;

import com.agenticsdd.knowledgeengine.embed.Embedder;
import com.agenticsdd.knowledgeengine.index.Fields;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hybrid BM25 + KNN-vector search over the Lucene index, fused with Reciprocal Rank Fusion (RRF)
 * so results are useful whether or not an embedding model is vendored — falls back to pure BM25
 * automatically when {@link Embedder#isAvailable()} is false.
 */
public final class LuceneSearcher implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(LuceneSearcher.class);
    private static final double RRF_K = 60.0;
    private static final int CANDIDATE_MULTIPLIER = 4;

    private final IndexReader reader;
    private final IndexSearcher searcher;
    private final Embedder embedder;

    public LuceneSearcher(final Path indexDir, final Embedder embedder) throws IOException {
        this.reader = DirectoryReader.open(FSDirectory.open(indexDir));
        this.searcher = new IndexSearcher(reader);
        this.embedder = embedder;
    }

    public List<SearchResult> search(final String queryText, final int limit) throws IOException, ParseException {
        final Map<String, RankedHit> byId = new LinkedHashMap<>();

        final TopDocs bm25 = runBm25(queryText, limit * CANDIDATE_MULTIPLIER);
        addRanked(bm25, byId, true);

        if (embedder.isAvailable()) {
            final float[] vector = embedder.embed(queryText);
            if (vector != null) {
                final TopDocs knn = runKnn(vector, limit * CANDIDATE_MULTIPLIER);
                addRanked(knn, byId, false);
            }
        } else {
            LOG.debug("No embedder available — returning BM25-only results");
        }

        return byId.values().stream()
                .sorted((a, b) -> Double.compare(b.fusedScore, a.fusedScore))
                .limit(limit)
                .map(this::toSearchResult)
                .toList();
    }

    private TopDocs runBm25(final String queryText, final int n) throws IOException, ParseException {
        final QueryParser parser = new QueryParser(Fields.CONTENT, new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        final Query query = parser.parse(QueryParser.escape(queryText).replace("\\ ", " "));
        return searcher.search(query, n);
    }

    private TopDocs runKnn(final float[] vector, final int n) throws IOException {
        final Query knnQuery = new KnnFloatVectorQuery(Fields.VECTOR, vector, n);
        return searcher.search(knnQuery, n);
    }

    private void addRanked(final TopDocs docs, final Map<String, RankedHit> byId, final boolean isBm25) throws IOException {
        final ScoreDoc[] hits = docs.scoreDocs;
        for (int rank = 0; rank < hits.length; rank++) {
            final Document document = searcher.storedFields().document(hits[rank].doc);
            final String id = document.get(Fields.ID);
            final double rrfContribution = 1.0 / (RRF_K + rank + 1);
            final RankedHit hit = byId.computeIfAbsent(id, k -> new RankedHit(document));
            hit.fusedScore += rrfContribution;
            if (isBm25) {
                hit.bm25Score = hits[rank].score;
            } else {
                hit.knnScore = hits[rank].score;
            }
        }
    }

    private SearchResult toSearchResult(final RankedHit hit) {
        final Document d = hit.document;
        final String content = d.get(Fields.CONTENT);
        final String snippet = content == null ? "" : content.substring(0, Math.min(400, content.length()));
        final String lineStr = d.get(Fields.LINE);
        final Integer line = lineStr == null ? null : Integer.parseInt(lineStr);
        return new SearchResult(
                d.get(Fields.ID),
                d.get(Fields.PATH),
                d.get(Fields.TYPE),
                d.get(Fields.SOURCE),
                d.get(Fields.TITLE),
                snippet,
                d.get(Fields.COMMUNITY),
                line,
                (float) hit.fusedScore
        );
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private static final class RankedHit {
        final Document document;
        double fusedScore;
        float bm25Score;
        float knnScore;

        RankedHit(final Document document) {
            this.document = document;
        }
    }
}
