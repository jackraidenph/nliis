package dev.jackraidenph.nliis.backend.data.language.model;

import dev.jackraidenph.nliis.backend.service.DocumentStatisticsService;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Setter

@Component
@Scope("prototype")
public class ShortWordsModel extends FrequentWordsModel {

    private long maxLength = 5;

    public ShortWordsModel(DocumentStatisticsService documentStatisticsService) {
        super(documentStatisticsService);
    }

    @Override
    protected boolean wordFilter(String word) {
        return word.length() <= this.maxLength;
    }
}
