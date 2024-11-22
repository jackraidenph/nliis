package dev.jackraidenph.nliis.backend.data.language.model;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.service.DocumentStatisticsService;
import dev.jackraidenph.nliis.backend.service.TFIDFService;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

@RequiredArgsConstructor
@Log

@Component
@Scope("prototype")
public class FrequentWordsModel implements LanguageModel {

    private final DocumentStatisticsService documentStatisticsService;
    private final Map<String, Map<String, Double>> localeTermsMap = new LinkedHashMap<>();

    @Setter
    private long maxEntries = 5;

    @Override
    public void fit(Document... documents) {
        log.info("Started building...");
        localeTermsMap.clear();
        DocumentRepository documentRepository = new DocumentRepository();
        documentRepository.addDocuments(documents);
        DocumentStatisticsRepository documentStatisticsRepository = new DocumentStatisticsRepository();
        Arrays.stream(documents).forEach(
                doc -> documentStatisticsRepository.addStatistics(
                        doc,
                        this.documentStatisticsService.computeDocumentStatistics(doc)
                )
        );
        TFIDFService tfidfService = new TFIDFService(documentRepository, documentStatisticsRepository);

        for (Document doc : documents) {
            if (doc.getLocale() == null) {
                continue;
            }
            this.localeTermsMap.put(
                    doc.getLocale().getLanguage(),
                    tfidfService.getTopK(
                            (int) this.maxEntries,
                            doc,
                            this::wordFilter
                    )
            );
        }
        log.info("Built!");
    }

    protected boolean wordFilter(String word) {
        return true;
    }

    @Override
    public String classify(String textToEvaluate) {
        log.info("Started extracting...");
        String sanitized = StringUtilities.sanitize(textToEvaluate);
        String[] words = sanitized.split(StringUtilities.ONE_OR_MORE_SPACES);
        log.info("Extracted!");

        log.info("Started counting...");
        Map<String, Double> matches = new HashMap<>();
        Arrays.stream(words).forEach(word -> {
            for (String locale : localeTermsMap.keySet()) {
                if (localeTermsMap.get(locale).containsKey(word)) {
                    double probability = Math.sqrt(1 + localeTermsMap.get(locale).get(word));
                    matches.compute(locale, (k, v) -> v == null ? probability : v * probability);
                }
            }
        });
        log.info("Counted!");

        return matches.entrySet().stream()
                .max(Entry.comparingByValue())
                .map(Entry::getKey)
                .orElse(LanguageModel.UNCLASSIFIED);
    }

    public Map<String, Map<String, Double>> getDictionary() {
        return Collections.unmodifiableMap(this.localeTermsMap);
    }
}
