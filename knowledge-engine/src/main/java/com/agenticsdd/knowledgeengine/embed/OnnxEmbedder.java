package com.agenticsdd.knowledgeengine.embed;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Sentence embedder backed by a locally vendored ONNX model (e.g. all-MiniLM-L6-v2), run via
 * ONNX Runtime with no network access. Expects a BERT-family model exporting
 * {@code input_ids}/{@code attention_mask}/{@code token_type_ids} inputs and a
 * {@code last_hidden_state} output, mean-pooled over non-padding tokens then L2-normalized —
 * the standard sentence-transformers recipe.
 *
 * <p>Model files are NOT fetched automatically (HuggingFace's model-download CDN is blocked by
 * this network's TLS-inspecting proxy). See models/README.md for how to vendor them manually.
 */
public final class OnnxEmbedder implements Embedder, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OnnxEmbedder.class);
    private static final int MAX_TOKENS = 256;

    private final int dimensions;
    private final OrtEnvironment env;
    private final OrtSession session;
    private final WordPieceTokenizer tokenizer;
    private final boolean available;

    private OnnxEmbedder(final OrtEnvironment env, final OrtSession session,
                          final WordPieceTokenizer tokenizer, final int dimensions) {
        this.env = env;
        this.session = session;
        this.tokenizer = tokenizer;
        this.dimensions = dimensions;
        this.available = true;
    }

    private OnnxEmbedder() {
        this.env = null;
        this.session = null;
        this.tokenizer = null;
        this.dimensions = 0;
        this.available = false;
    }

    /**
     * Loads the model at {@code modelDir}, expecting {@code model.onnx} and {@code vocab.txt}.
     * Returns an unavailable embedder (not a thrown exception) if the files are missing, so
     * callers can fall back to BM25-only search without special-casing "no model yet".
     */
    public static Embedder load(final Path modelDir, final int dimensions) {
        final Path modelFile = modelDir.resolve("model.onnx");
        final Path vocabFile = modelDir.resolve("vocab.txt");
        if (!Files.exists(modelFile) || !Files.exists(vocabFile)) {
            LOG.warn("No ONNX model vendored at {} (expected model.onnx + vocab.txt) — "
                    + "vector search disabled, BM25 lexical search still works. See models/README.md.", modelDir);
            return new OnnxEmbedder();
        }
        try {
            final OrtEnvironment env = OrtEnvironment.getEnvironment();
            final OrtSession session = env.createSession(modelFile.toString(), new OrtSession.SessionOptions());
            final WordPieceTokenizer tokenizer = new WordPieceTokenizer(vocabFile);
            LOG.info("Loaded ONNX embedding model from {}", modelFile);
            return new OnnxEmbedder(env, session, tokenizer, dimensions);
        } catch (OrtException | IOException e) {
            LOG.warn("Failed to load ONNX model at {}: {} — vector search disabled", modelFile, e.getMessage());
            return new OnnxEmbedder();
        }
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public float[] embed(final String text) {
        if (!available) {
            return null;
        }
        final int[] ids = tokenizer.encode(text, MAX_TOKENS);
        final long[] inputIds = new long[ids.length];
        final long[] attentionMask = new long[ids.length];
        final long[] tokenTypeIds = new long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            inputIds[i] = ids[i];
            attentionMask[i] = 1L;
            tokenTypeIds[i] = 0L;
        }
        final long[] shape = {1, ids.length};
        try (OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape);
             OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape);
             OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIds), shape)) {

            final Map<String, OnnxTensor> inputs = Map.of(
                    "input_ids", inputIdsTensor,
                    "attention_mask", attentionMaskTensor,
                    "token_type_ids", tokenTypeIdsTensor);

            try (OrtSession.Result result = session.run(inputs)) {
                final float[][][] lastHiddenState = (float[][][]) result.get(0).getValue();
                return meanPoolAndNormalize(lastHiddenState[0], attentionMask);
            }
        } catch (OrtException e) {
            LOG.warn("Embedding failed for input of length {}: {}", text.length(), e.getMessage());
            return null;
        }
    }

    /** Mean-pools token embeddings over non-padding positions, then L2-normalizes — sentence-transformers recipe. */
    private float[] meanPoolAndNormalize(final float[][] tokenEmbeddings, final long[] attentionMask) {
        final int hidden = tokenEmbeddings[0].length;
        final float[] pooled = new float[hidden];
        long validTokens = 0;
        for (int t = 0; t < tokenEmbeddings.length; t++) {
            if (attentionMask[t] == 0L) {
                continue;
            }
            validTokens++;
            for (int h = 0; h < hidden; h++) {
                pooled[h] += tokenEmbeddings[t][h];
            }
        }
        final long divisor = Math.max(1, validTokens);
        double norm = 0;
        for (int h = 0; h < hidden; h++) {
            pooled[h] /= divisor;
            norm += (double) pooled[h] * pooled[h];
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int h = 0; h < hidden; h++) {
                pooled[h] /= (float) norm;
            }
        }
        return pooled;
    }

    @Override
    public void close() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (OrtException e) {
            LOG.warn("Failed to close ONNX session: {}", e.getMessage());
        }
    }
}
