package com.scripto.backend.classification.local;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DifficultyFeatureExtractor {
    private static final List<String> ADVANCED_MARKERS = List.of(
            "trade-off", "tradeoff", "complexity", "optimization", "distributed",
            "architecture", "internals", "concurrency", "scalability", "performance",
            "compromise", "otimizacao", "otimização", "distribuido", "distribuído",
            "arquitetura", "concorrencia", "concorrência", "escalabilidade", "desempenho"
    );
    private static final List<String> BEGINNER_MARKERS = List.of(
            "introduction", "basic", "fundamental", "what is", "getting started",
            "introducao", "introdução", "basico", "básico", "fundamento", "o que e", "o que é"
    );
    private static final List<String> CODE_MARKERS = List.of(
            "```", "{", "}", ";", "=>", "public ", "private ", "class ", "def ",
            "function ", "SELECT ", "INSERT ", "UPDATE ", "curl ", "npm ", "pip "
    );
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[{}()\\[\\];:=<>]");

    public float[] extract(String title, String content) {
        String text = ((title == null ? "" : title) + "\n" + (content == null ? "" : content)).trim();
        Matcher wordMatcher = WORD_PATTERN.matcher(text);
        int wordCountRaw = 0;
        Set<String> uniqueWords = new HashSet<>();
        while (wordMatcher.find()) {
            wordCountRaw++;
            uniqueWords.add(wordMatcher.group().toLowerCase(Locale.ROOT));
        }
        int wordCount = Math.max(wordCountRaw, 1);
        long sentenceCount = Pattern.compile("[.!?]+").splitAsStream(text).filter(part -> !part.isBlank()).count();
        double averageSentenceLength = (double) wordCount / Math.max(sentenceCount, 1L);
        double lexicalDiversity = (double) uniqueWords.size() / wordCount;
        double codeMarkerDensity = (double) countOccurrences(text, CODE_MARKERS) / wordCount;
        double advancedMarkerDensity = (double) countOccurrences(text, ADVANCED_MARKERS) / wordCount;
        double beginnerMarkerDensity = (double) countOccurrences(text, BEGINNER_MARKERS) / wordCount;
        String[] lines = text.split("\\R", -1);
        long headings = java.util.Arrays.stream(lines)
                .map(String::strip)
                .filter(line -> line.startsWith("#") || line.startsWith("-") || line.startsWith("*"))
                .count();
        double headingDensity = (double) headings / Math.max(lines.length, 1);
        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(text);
        int symbols = 0;
        while (symbolMatcher.find()) {
            symbols++;
        }
        double symbolDensity = (double) symbols / Math.max(text.length(), 1);

        return new float[]{
                (float) Math.log1p(wordCount),
                (float) averageSentenceLength,
                (float) lexicalDiversity,
                (float) codeMarkerDensity,
                (float) advancedMarkerDensity,
                (float) beginnerMarkerDensity,
                (float) headingDensity,
                (float) symbolDensity
        };
    }

    private int countOccurrences(String text, List<String> markers) {
        String lowered = text.toLowerCase(Locale.ROOT);
        int count = 0;
        for (String marker : markers) {
            String needle = marker.toLowerCase(Locale.ROOT);
            int from = 0;
            while ((from = lowered.indexOf(needle, from)) >= 0) {
                count++;
                from += needle.length();
            }
        }
        return count;
    }
}
