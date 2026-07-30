package com.muniai.conversation.application;

import com.muniai.document.application.VectorStore;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CitationAligner {
    private static final Pattern MODEL_CITATION =
            Pattern.compile("\\s*\\[\\s*Source\\s+\\d+\\s*]", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N}_/-]*");
    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[.!?])\\s+");
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "according", "after", "also", "and", "are", "based", "been", "before",
            "being", "between", "both", "but", "can", "could", "document", "does", "each",
            "for", "from", "had", "has", "have", "how", "into", "its", "may", "means",
            "mentioned", "more", "most", "not", "of", "on", "only", "or", "other", "our",
            "should", "that", "the", "their", "then", "there", "these", "they", "this",
            "through", "to", "use", "used", "using", "was", "were", "what", "when", "where",
            "which", "while", "will", "with", "would", "you", "your"
    );

    Alignment align(String modelAnswer, List<VectorStore.SearchHit> sources) {
        String cleaned = MODEL_CITATION.matcher(modelAnswer).replaceAll("").strip();
        if (cleaned.isEmpty() || sources.isEmpty()) return new Alignment(cleaned, Set.of());

        List<SourceTerms> sourceTerms = new ArrayList<>();
        Map<String, Integer> documentFrequency = new HashMap<>();
        for (int index = 0; index < sources.size(); index++) {
            List<String> terms = terms(sources.get(index).content());
            Set<String> unique = new HashSet<>(terms);
            unique.forEach(term -> documentFrequency.merge(term, 1, Integer::sum));
            sourceTerms.add(new SourceTerms(index + 1, terms, unique, sources.get(index).score()));
        }

        Set<Integer> cited = new LinkedHashSet<>();
        List<String> alignedParagraphs = new ArrayList<>();
        for (String paragraph : cleaned.split("\\R\\s*\\R")) {
            List<String> alignedSentences = new ArrayList<>();
            for (String sentence : SENTENCE_BOUNDARY.split(paragraph.strip())) {
                if (sentence.isBlank()) continue;
                OptionalInt source = bestSource(sentence, sourceTerms, documentFrequency);
                if (source.isPresent()) {
                    int sourceIndex = source.getAsInt();
                    cited.add(sourceIndex);
                    alignedSentences.add(sentence.strip() + " [Source " + sourceIndex + "]");
                } else {
                    alignedSentences.add(sentence.strip());
                }
            }
            if (!alignedSentences.isEmpty()) alignedParagraphs.add(String.join(" ", alignedSentences));
        }
        return new Alignment(String.join("\n\n", alignedParagraphs), Set.copyOf(cited));
    }

    private OptionalInt bestSource(String sentence, List<SourceTerms> sources,
                                   Map<String, Integer> documentFrequency) {
        List<String> claimTerms = terms(sentence);
        if (claimTerms.isEmpty()) return OptionalInt.empty();
        double bestScore = 0;
        int bestIndex = -1;
        for (SourceTerms source : sources) {
            double score = 0;
            int matches = 0;
            for (String term : new HashSet<>(claimTerms)) {
                if (source.uniqueTerms().contains(term)) {
                    int frequency = documentFrequency.getOrDefault(term, sources.size());
                    score += 1.0 + Math.log((sources.size() + 1.0) / (frequency + 1.0));
                    matches++;
                }
            }
            score += matchingBigrams(claimTerms, source.terms()) * 1.5;
            score += source.retrievalScore() * 0.001;
            if (matches > 0 && score > bestScore) {
                bestScore = score;
                bestIndex = source.index();
            }
        }
        return bestIndex < 0 ? OptionalInt.empty() : OptionalInt.of(bestIndex);
    }

    private int matchingBigrams(List<String> claim, List<String> source) {
        if (claim.size() < 2 || source.size() < 2) return 0;
        Set<String> sourceBigrams = new HashSet<>();
        for (int i = 0; i < source.size() - 1; i++) {
            sourceBigrams.add(source.get(i) + "\u0000" + source.get(i + 1));
        }
        int matches = 0;
        for (int i = 0; i < claim.size() - 1; i++) {
            if (sourceBigrams.contains(claim.get(i) + "\u0000" + claim.get(i + 1))) matches++;
        }
        return matches;
    }

    private List<String> terms(String value) {
        List<String> result = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(value.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String term = matcher.group();
            if (term.length() >= 3 && !STOP_WORDS.contains(term)) result.add(term);
        }
        return result;
    }

    record Alignment(String answer, Set<Integer> citedSourceIndexes) {}
    private record SourceTerms(int index, List<String> terms, Set<String> uniqueTerms,
                               double retrievalScore) {}
}
