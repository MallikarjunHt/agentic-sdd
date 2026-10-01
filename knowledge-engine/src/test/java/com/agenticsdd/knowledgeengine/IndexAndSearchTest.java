package com.agenticsdd.knowledgeengine;

import com.agenticsdd.knowledgeengine.embed.NoopEmbedder;
import com.agenticsdd.knowledgeengine.index.IndexedDocument;
import com.agenticsdd.knowledgeengine.index.LuceneIndex;
import com.agenticsdd.knowledgeengine.search.LuceneSearcher;
import com.agenticsdd.knowledgeengine.search.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexAndSearchTest {

    @Test
    void indexesAndFindsADocumentByKeyword(@TempDir final Path indexDir) throws Exception {
        try (LuceneIndex index = new LuceneIndex(indexDir, new NoopEmbedder())) {
            index.upsert(IndexedDocument.doc("doc-1", "readme.md", "test-source", "README",
                    "This service handles OTP verification and issues OAuth2 tokens."));
            index.upsert(IndexedDocument.codeNode("code-1", "OtpService.java", "test-source", "OtpService",
                    "OtpService verifyOtp method", "42", 77));
            index.commit();
        }

        try (LuceneSearcher searcher = new LuceneSearcher(indexDir, new NoopEmbedder())) {
            final List<SearchResult> results = searcher.search("OTP verification", 10);
            assertFalse(results.isEmpty(), "expected at least one hit for 'OTP verification'");
            assertTrue(results.stream().anyMatch(r -> r.id().equals("doc-1")));
        }
    }

    @Test
    void upsertReplacesRatherThanDuplicatesById(@TempDir final Path indexDir) throws Exception {
        try (LuceneIndex index = new LuceneIndex(indexDir, new NoopEmbedder())) {
            index.upsert(IndexedDocument.doc("dup-1", "a.md", "src", "A", "first version of the content"));
            index.upsert(IndexedDocument.doc("dup-1", "a.md", "src", "A", "second version of the content"));
            index.commit();
        }

        try (LuceneSearcher searcher = new LuceneSearcher(indexDir, new NoopEmbedder())) {
            final List<SearchResult> results = searcher.search("version", 10);
            final long matchingDupOne = results.stream().filter(r -> r.id().equals("dup-1")).count();
            assertEquals(1, matchingDupOne, "upsert must replace the existing doc, not duplicate it");
        }
    }

    @Test
    void codeNodeCarriesCommunityAndLineForWikiGeneration(@TempDir final Path indexDir) throws Exception {
        try (LuceneIndex index = new LuceneIndex(indexDir, new NoopEmbedder())) {
            index.upsert(IndexedDocument.codeNode("code-2", "Foo.java", "src", "Foo", "class Foo", "7", 15));
            index.commit();
        }

        try (LuceneSearcher searcher = new LuceneSearcher(indexDir, new NoopEmbedder())) {
            final List<SearchResult> results = searcher.search("Foo", 10);
            final SearchResult hit = results.stream().filter(r -> r.id().equals("code-2")).findFirst().orElseThrow();
            assertEquals("7", hit.community());
            assertEquals(15, hit.line());
        }
    }
}
