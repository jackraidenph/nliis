package dev.jackraidenph.nliis.backend.service.languagedetection;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.language.model.FrequentWordsModel;
import dev.jackraidenph.nliis.backend.data.language.model.ShortWordsModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Log

@Service
public class ShortWordsLanguageDetectionService implements LanguageDetectionService {

    private final ShortWordsModel shortWordsModel;
    private boolean wasFit = false;

    public void setMaxEntries(long size) {
        this.shortWordsModel.setMaxEntries(size);
    }

    public void setMaxWodLength(int length) {
        this.shortWordsModel.setMaxLength(length);
    }

    public void fitModel(Document... documents) {
        this.shortWordsModel.fit(documents);
        this.wasFit = true;
    }

    public Map<String, Map<String, Double>> modelDictionary() {
        return this.shortWordsModel.getDictionary();
    }

    @Override
    public String predict(String text) {
        if (!wasFit) {
            log.warning("Trying to predict language of a text using unfit model!");
        }

        return this.shortWordsModel.classify(text);
    }
}
