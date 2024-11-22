package dev.jackraidenph.nliis.backend.service;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log
@Service
public class DocumentStatisticsService {

    private static final Pattern MINIMAL_WORD_PATTERN = Pattern.compile("\\p{L}+");

    public DocumentStatistics computeDocumentStatistics(Document document) {
        log.info("Started sanitizing...");
        String documentText = StringUtilities.sanitize(document.getContent());
        log.info("Sanitized!");

        log.info("Started computing...");
        AtomicLong totalWords = new AtomicLong();
        Map<String, Long> map = Arrays.stream(documentText.split(StringUtilities.MATCH_NEW_LINE))
                .flatMap(line -> Arrays.stream(line.split(StringUtilities.ONE_OR_MORE_SPACES)))
                .filter(word -> MINIMAL_WORD_PATTERN.matcher(word).find())
                .peek(w -> totalWords.getAndIncrement())
                .parallel()
                .collect(
                        Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()
                        )
                );
        List<String> ordered = map.entrySet().stream()
                .sorted(Comparator.comparingLong((Entry<String, Long> e) -> e.getValue()).reversed())
                .map(Entry::getKey)
                .toList();
        log.info("Computed...");

        return new DocumentStatistics(totalWords.get(), ordered, map);
    }

}
