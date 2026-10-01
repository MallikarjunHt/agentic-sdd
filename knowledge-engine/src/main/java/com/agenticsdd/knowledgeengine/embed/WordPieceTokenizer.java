package com.agenticsdd.knowledgeengine.embed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal WordPiece tokenizer compatible with BERT-family sentence-transformer models
 * (e.g. all-MiniLM-L6-v2). Loads a vocab.txt (one token per line, line number = token id).
 * Not a full HuggingFace tokenizer re-implementation — handles lowercasing, basic punctuation
 * splitting, and greedy longest-match-first WordPiece, which covers the common case.
 */
public final class WordPieceTokenizer {

    private static final String UNK = "[UNK]";
    private static final String CLS = "[CLS]";
    private static final String SEP = "[SEP]";
    private static final String CONTINUATION_PREFIX = "##";
    private static final int MAX_INPUT_CHARS_PER_WORD = 200;

    private final Map<String, Integer> vocab;
    private final int unkId;
    private final int clsId;
    private final int sepId;

    public WordPieceTokenizer(final Path vocabFile) throws IOException {
        this.vocab = new HashMap<>();
        final List<String> lines = Files.readAllLines(vocabFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            vocab.put(lines.get(i), i);
        }
        this.unkId = requireId(UNK);
        this.clsId = requireId(CLS);
        this.sepId = requireId(SEP);
    }

    private int requireId(final String token) {
        final Integer id = vocab.get(token);
        if (id == null) {
            throw new IllegalStateException("vocab.txt is missing required special token: " + token);
        }
        return id;
    }

    /** Tokenizes text into input ids, prefixed with [CLS] and suffixed with [SEP]. */
    public int[] encode(final String text, final int maxTokens) {
        final List<Integer> ids = new ArrayList<>();
        ids.add(clsId);
        for (final String word : basicTokenize(text)) {
            for (final String piece : wordPiece(word)) {
                if (ids.size() >= maxTokens - 1) {
                    break;
                }
                ids.add(vocab.getOrDefault(piece, unkId));
            }
        }
        ids.add(sepId);
        return ids.stream().mapToInt(Integer::intValue).toArray();
    }

    private List<String> basicTokenize(final String text) {
        final String lower = text.toLowerCase(Locale.ROOT);
        final List<String> tokens = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < lower.length(); i++) {
            final char c = lower.charAt(i);
            if (Character.isWhitespace(c)) {
                flush(current, tokens);
            } else if (isPunctuation(c)) {
                flush(current, tokens);
                tokens.add(String.valueOf(c));
            } else {
                current.append(c);
            }
        }
        flush(current, tokens);
        return tokens;
    }

    private void flush(final StringBuilder current, final List<String> tokens) {
        if (current.length() > 0) {
            tokens.add(current.toString());
            current.setLength(0);
        }
    }

    private boolean isPunctuation(final char c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);
    }

    private List<String> wordPiece(final String word) {
        if (word.length() > MAX_INPUT_CHARS_PER_WORD) {
            return List.of(UNK);
        }
        final List<String> pieces = new ArrayList<>();
        int start = 0;
        while (start < word.length()) {
            int end = word.length();
            String matched = null;
            while (start < end) {
                String candidate = word.substring(start, end);
                if (start > 0) {
                    candidate = CONTINUATION_PREFIX + candidate;
                }
                if (vocab.containsKey(candidate)) {
                    matched = candidate;
                    break;
                }
                end--;
            }
            if (matched == null) {
                return List.of(UNK);
            }
            pieces.add(matched);
            start = end;
        }
        return pieces;
    }
}
