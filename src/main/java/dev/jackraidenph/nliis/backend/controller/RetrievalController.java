package dev.jackraidenph.nliis.backend.controller;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;
import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.service.DocumentStatisticsService;
import dev.jackraidenph.nliis.backend.service.TFIDFService;
import dev.jackraidenph.nliis.backend.service.retrieval.BM25RetrievalService;
import dev.jackraidenph.nliis.backend.service.retrieval.RetrievalService;
import dev.jackraidenph.nliis.backend.service.retrieval.RetrievalService.DocumentScore;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RetrievalController {

    private final RetrievalService bm25RetrievalService;
    private final DocumentRepository documentRepository;
    private final DocumentStatisticsService documentStatisticsService;
    private final DocumentStatisticsRepository documentStatisticsRepository;

    public RetrievalController(
            DocumentRepository documentRepository,
            DocumentStatisticsService documentStatisticsService,
            DocumentStatisticsRepository documentStatisticsRepository
    ) {
        this.bm25RetrievalService = new BM25RetrievalService(
                documentRepository,
                documentStatisticsRepository,
                new TFIDFService(documentRepository, documentStatisticsRepository)
        );
        this.documentRepository = documentRepository;
        this.documentStatisticsService = documentStatisticsService;
        this.documentStatisticsRepository = documentStatisticsRepository;
    }

    public List<DocumentScore> retrieveDocuments(String query) {
        if (query.isBlank()) {
            return List.of();
        }

        String sanitized = StringUtilities.sanitize(query);

        return this.bm25RetrievalService.retrieve(sanitized)
                .stream()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    public Map<Document, DocumentStatistics> processDocuments(Iterable<Document> documents) {
        Map<Document, DocumentStatistics> res = new HashMap<>();
        for (Document document : documents) {
            DocumentStatistics statistics = this.documentStatisticsService.computeDocumentStatistics(document);
            documentStatisticsRepository.addStatistics(document, statistics);
            res.put(document, statistics);
        }

        return res;
    }

    public void addDocumentsToRepository(Document... documents) {
        this.documentRepository.addDocuments(documents);
    }

    public void addDocumentsToRepository(Iterable<Document> documents) {
        this.documentRepository.addDocuments(documents);
    }

    public void clearRepository() {
        this.documentRepository.clear();
    }
}
