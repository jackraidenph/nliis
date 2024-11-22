package dev.jackraidenph.nliis.backend.service.retrieval;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;
import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.service.TFIDFService;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;

@RequiredArgsConstructor
public class BM25RetrievalService implements RetrievalService {

    @Setter
    private double k1 = 1.5, b = 0.75;

    private final DocumentRepository documentRepository;
    private final DocumentStatisticsRepository documentStatisticsRepository;
    private final TFIDFService tfidfService;

    private long getDocumentLength(Document document) {
        Optional<DocumentStatistics> statistics = this.documentStatisticsRepository.getStatistic(document);
        return statistics.map(DocumentStatistics::totalWords).orElse(0L);
    }

    private long averageDocumentLength() {
        return (long) this.documentRepository.getDocuments().stream()
                .mapToLong(this::getDocumentLength)
                .average()
                .orElse(0.);
    }

    private DocumentScore getDocumentScore(String query, Document document) {
        String sanitizedQuery = StringUtilities.sanitize(query);
        String[] terms = sanitizedQuery.split("\\s+");

        double score = 0.;

        for (String term : terms) {
            double idf = this.tfidfService.getTermInvertedDocumentFrequency(term);
            long tf = this.tfidfService.getTimesTermIn(term, document);
            double normDocLength = this.getDocumentLength(document) / (double) this.averageDocumentLength();

            double i = tf * (k1 + 1);

            double j = tf + k1 * (1 - b + b * normDocLength);

            score += idf * (i / j);
        }

        return new DocumentScore(document, score);
    }

    @Override
    public List<DocumentScore> retrieve(String query) {
        return this.documentRepository.getDocuments()
                .stream()
                .filter(this.documentStatisticsRepository::hasStatistic)
                .map(doc -> this.getDocumentScore(query, doc))
                .toList();
    }
}
