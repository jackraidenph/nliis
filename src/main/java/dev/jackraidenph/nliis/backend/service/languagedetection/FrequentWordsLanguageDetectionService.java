package dev.jackraidenph.nliis.backend.service.languagedetection;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.language.model.FrequentWordsModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Log

@Service
public class FrequentWordsLanguageDetectionService implements LanguageDetectionService {

    private final FrequentWordsModel frequentWordsModel;
    private boolean wasFit = false;

    public void setMaxEntries(long size) {
        this.frequentWordsModel.setMaxEntries(size);
    }

    public void fitModel(Document... documents) {
        this.frequentWordsModel.fit(documents);
        this.wasFit = true;
    }

    public Map<String, Map<String, Double>> modelDictionary() {
        return this.frequentWordsModel.getDictionary();
    }

    @Override
    public String predict(String text) {
        if (!wasFit) {
            log.warning("Trying to predict language of a text using unfit model!");
        }

        return this.frequentWordsModel.classify(text);
    }
}
