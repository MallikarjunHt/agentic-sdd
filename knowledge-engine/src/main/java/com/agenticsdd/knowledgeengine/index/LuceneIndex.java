package com.agenticsdd.knowledgeengine.index;

import com.agenticsdd.knowledgeengine.embed.Embedder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/** Owns the on-disk Lucene index: opens it, and exposes a single method to add/update a document. */
public final class LuceneIndex implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(LuceneIndex.class);

    private final Directory directory;
    private final IndexWriter writer;
    private final Embedder embedder;

    public LuceneIndex(final Path indexDir, final Embedder embedder) throws IOException {
        this.directory = FSDirectory.open(indexDir);
        final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        this.writer = new IndexWriter(directory, config);
        this.embedder = embedder;
    }

    /**
     * Adds or replaces a document by {@code id}. Content is embedded into a KNN vector field
     * if the embedder is available; otherwise the document is still fully searchable via BM25.
     */
    public void upsert(final IndexedDocument doc) throws IOException {
        final Document lucene = new Document();
        lucene.add(new StringField(Fields.ID, doc.id(), Field.Store.YES));
        lucene.add(new StringField(Fields.PATH, doc.path(), Field.Store.YES));
        lucene.add(new StringField(Fields.TYPE, doc.type(), Field.Store.YES));
        lucene.add(new StringField(Fields.SOURCE, doc.source(), Field.Store.YES));
        lucene.add(new TextField(Fields.TITLE, doc.title(), Field.Store.YES));
        lucene.add(new TextField(Fields.CONTENT, doc.content(), Field.Store.YES));
        if (doc.community() != null) {
            lucene.add(new StringField(Fields.COMMUNITY, doc.community(), Field.Store.YES));
        }
        if (doc.line() != null) {
            lucene.add(new StoredField(Fields.LINE, String.valueOf(doc.line())));
        }

        if (embedder.isAvailable()) {
            final float[] vector = embedder.embed(doc.title() + "\n" + doc.content());
            if (vector != null) {
                lucene.add(new KnnFloatVectorField(Fields.VECTOR, vector, VectorSimilarityFunction.COSINE));
            }
        }

        writer.updateDocument(new org.apache.lucene.index.Term(Fields.ID, doc.id()), lucene);
    }

    public void commit() throws IOException {
        writer.commit();
        LOG.info("Committed index at {}", directory);
    }

    @Override
    public void close() throws IOException {
        writer.close();
        directory.close();
    }
}
