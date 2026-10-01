# knowledge-engine

A Lucene-based BM25/vector search CLI, vendored here as source (not a binary)
so `agentic-sdd` has a knowledge base that works with zero external MCP
servers. Renamed from its origin project's package/groupId
(`com.agenticsdd.knowledgeengine`, was a different `au.com.*` namespace) —
same MIT license, copyright notice preserved in `LICENSE`.

## Build (one-time, done automatically by `/sdd.init`)

```bash
mvn -q -DskipTests package   # needs Java 21+; produces target/knowledge-engine.jar
```

## Use

```bash
java -jar target/knowledge-engine.jar index  --source <label> --index-dir <abs-path> --models-dir <abs-path> <path-to-index>
java -jar target/knowledge-engine.jar search "<query>" --index-dir <abs-path> --models-dir <abs-path> --limit 10
java -jar target/knowledge-engine.jar doctor  --index-dir <abs-path> --models-dir <abs-path>
```

Always pass `--index-dir`/`--models-dir` as **absolute** paths — both default
to a cwd-relative path, which silently breaks if the invoking process's
working directory isn't what you expect.

## No embedding model ships here

`--models-dir` is expected to contain `model.onnx` + `vocab.txt`
(all-MiniLM-L6-v2, 384 dimensions). Without them, every command still works —
`doctor` reports `BM25 only`, `index`/`search` just skip vector scoring. To
add vector search later, fetch the model from a mirror that isn't blocked by
most corporate proxies:

```
https://storage.googleapis.com/qdrant-fastembed/sentence-transformers-all-MiniLM-L6-v2.tar.gz
```

Extract `model.onnx` + `vocab.txt` from the `fast-all-MiniLM-L6-v2/` folder
inside, drop them into your configured `modelsDir`. Nothing else to
reconfigure — `EmbedderFactory` picks them up automatically on the next run.
