# Scripto Java integration

Primary runtime file: `scripto_runtime_bundle.json`.

1. Load the bundle once during application startup.
2. Build the exact text `[TITLE]\n{title}\n\n[CONTENT]\n{content}`.
3. Tokenize with the files in `embedding/`, truncating to 128 tokens.
4. Provide every input declared in `embedding/onnx_contract.json` as INT64 tensors.
5. Execute `embedding/model.onnx` and read the `sentence_embedding` output.
6. Do not apply pooling or normalization in Java; both operations are embedded in the ONNX graph.
7. Calculate category probabilities from the exported linear weights.
8. Calculate maximum cosine similarity against category centroids.
9. Extract the difficulty features in the order declared by the bundle, standardize them, and apply its linear model.
10. Calculate independent sigmoid probabilities for tags and return at most 5.
11. Apply every threshold in `acceptance_policy`.
12. Call Nemotron only when the local result is invalid or the local runtime raises an error.
13. Store `model_version`, confidences and fallback reasons in inference telemetry.

The user text must not be translated before local inference. Categories, difficulties and tags are canonical English values. The frontend may localize category labels for display.
