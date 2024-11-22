package dev.jackraidenph.nliis.backend.service;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;
import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TFIDFService {

    private final DocumentRepository documentRepository;
    private final DocumentStatisticsRepository documentStatisticsRepository;

    private int getTotalDocuments() {
        return this.documentRepository.getTotalDocuments();
    }

    public int getNumberOfDocumentsContaining(String term) {
        return (int) this.documentRepository.getDocuments().stream()
                .map(this.documentStatisticsRepository::getStatistic)
                .filter(opt -> opt.isPresent() && opt.get().wordsUsage(term) > 0)
                .count();
    }

    public double getTFIDF(String term, Document document) {
        if (!this.documentStatisticsRepository.hasStatistic(document)) {
            throw new IllegalArgumentException("No statistics present for the document [%s]".formatted(document.getTitle()));
        }
        double tf = this.getTermRelativeFrequency(term, document);
        double idf = this.getTermInvertedDocumentFrequency(term);
        return tf * idf;
    }

    public double getTermRelativeFrequency(String term, Document document) {
        if (!this.documentStatisticsRepository.hasStatistic(document)) {
            throw new IllegalArgumentException("No statistics present for the document [%s]".formatted(document.getTitle()));
        }
        DocumentStatistics statistics = this.documentStatisticsRepository.getStatistic(document).get();
        long termFrequency = statistics.wordsUsage(term);
        long sum = statistics.totalWords();

        return termFrequency / (double) sum;
    }

    public double getModifiedTFIDF(String term, Document document) {
        if (!this.documentStatisticsRepository.hasStatistic(document)) {
            throw new IllegalArgumentException("No statistics present for the document [%s]".formatted(document.getTitle()));
        }
        DocumentStatistics statistics = this.documentStatisticsRepository.getStatistic(document).get();
        long timesIn = this.getTimesTermIn(term, document);
        long maxTerm = statistics.wordsUsage(statistics.ordered().getFirst());
        double maxRel = 1 + timesIn / (double) maxTerm;
        return 0.5 * maxRel * Math.log(this.getTotalDocuments() / (timesIn + 1.));
    }

    public double getTermInvertedDocumentFrequency(String term) {
        int N = this.getTotalDocuments();
        int nQi = this.getNumberOfDocumentsContaining(term);

        return Math.log(((N - nQi + 0.5) / (nQi + 0.5)) + 1);
    }

    public double sentenceScore(String sentence, Document document, boolean modified) {
        String sanitized = StringUtilities.sanitize(sentence);
        double score = 0;
        for (String word : sanitized.split(StringUtilities.ONE_OR_MORE_SPACES)) {
            score += modified ? this.getModifiedTFIDF(word, document) : this.getTFIDF(word, document);
        }
        return score;
    }

    public double sentenceScore(String sentence, Document document) {
        return this.sentenceScore(sentence, document, false);
    }

    public Map<String, Double> getTopK(int k, Document document, Predicate<String> wordPredicate, boolean modified) {
        Optional<DocumentStatistics> statistics = this.documentStatisticsRepository.getStatistic(document);
        if (statistics.isEmpty()) {
            throw new IllegalArgumentException("Document has no statistics in provided statistics repository!");
        }

        return statistics.get()
                .wordsUsage()
                .keySet()
                .stream()
                .filter(wordPredicate)
                .sorted(Comparator.comparingDouble(
                        word -> modified
                                ? this.getModifiedTFIDF((String) word, document)
                                : this.getTFIDF((String) word, document)
                ).reversed())
                .limit(k)
                .collect(Collectors.toMap(
                                Function.identity(),
                                word -> modified
                                        ? this.getModifiedTFIDF(word, document)
                                        : this.getTFIDF(word, document)
                        )
                );
    }

    public Map<String, Double> getTopK(int k, Document document, boolean modified) {
        return this.getTopK(k, document, word -> true, modified);
    }

    public Map<String, Double> getTopK(int k, Document document, Predicate<String> wordPredicate) {
        return this.getTopK(k, document, wordPredicate, false);
    }

    public Map<String, Double> getTopK(int k, Document document) {
        return this.getTopK(k, document, word -> true, false);
    }

    public long getTimesTermIn(String term, Document document) {
        Optional<DocumentStatistics> statistics = this.documentStatisticsRepository.getStatistic(document);
        return statistics.map(stats -> stats.wordsUsage(term)).orElse(0L);
    }

}
