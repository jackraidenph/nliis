package dev.jackraidenph.nliis.backend.data.document;

import java.util.List;
import java.util.Map;

public record DocumentStatistics(long totalWords, List<String> ordered, Map<String, Long> wordsUsage) {
    public long wordsUsage(String word) {
        if (!this.wordsUsage().containsKey(word)) {
            return 0L;
        }
        return this.wordsUsage().get(word);
    }

    public int getIndex(String word) {
        return ordered.indexOf(word);
    }
}
