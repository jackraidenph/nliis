package dev.jackraidenph.nliis.backend.service;


import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;
import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.service.SentenceDetectorService.Sentence;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class SummaryService {

    private final DocumentStatisticsService documentStatisticsService;
    private final SentenceDetectorService sentenceDetector;
    private int maxEntries = 10;
    private int minWordLength = 6;
    private int minSentenceLength = minWordLength * 12;

    public Summary getDocumentSummary(Document document) {

        DocumentRepository documentRepository = new DocumentRepository(document);
        DocumentStatisticsRepository documentStatisticsRepository = new DocumentStatisticsRepository();
        DocumentStatistics statistics = this.documentStatisticsService.computeDocumentStatistics(document);
        documentStatisticsRepository.addStatistics(document, statistics);
        TFIDFService tfidfService = new TFIDFService(documentRepository, documentStatisticsRepository);

        List<Sentence> sentences = this.sentenceDetector.splitIntoSentences(document.getContent());

        List<String> topKSentences = sentences.stream()
                .filter(sentence -> sentence.text().length() >= minSentenceLength)
                .sorted(
                        Comparator.comparingDouble(
                                (Sentence sentence) -> {
                                    double modTfIdf = tfidfService.sentenceScore(sentence.text(), document, true);
                                    double posD = 1 - sentence.span().getStart() / (double) document.getLength();

                                    return modTfIdf * posD;
                                }
                        ).reversed()
                )
                .parallel()
                .limit(this.maxEntries)
                .map(Sentence::text)
                .toList();

        List<String> topKWords = new ArrayList<>(
                tfidfService.getTopK(
                        this.maxEntries,
                        document,
                        w -> w.length() >= minWordLength,
                        false
                ).keySet()
        );

        return new Summary(topKWords, topKSentences);
    }

    public record Summary(List<String> keywords, List<String> sentences) {

    }

}
