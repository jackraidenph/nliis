package dev.jackraidenph.nliis.backend.repository;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DocumentStatisticsRepository {

    private final Map<Document, DocumentStatistics> statisticsMap;

    public DocumentStatisticsRepository(int capacity) {
        this.statisticsMap = new HashMap<>(capacity);
    }

    public DocumentStatisticsRepository() {
        this(16);
    }

    public void addStatistics(Document document, DocumentStatistics documentStatistics) {
        this.statisticsMap.put(document, documentStatistics);
    }

    public Optional<DocumentStatistics> getStatistic(Document document) {
        return Optional.ofNullable(this.statisticsMap.get(document));
    }

    public boolean hasStatistic(Document document) {
        return this.statisticsMap.containsKey(document) && this.statisticsMap.get(document) != null;
    }

}
