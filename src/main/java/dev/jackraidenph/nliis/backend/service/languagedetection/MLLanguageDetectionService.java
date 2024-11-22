package dev.jackraidenph.nliis.backend.service.languagedetection;

import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.springframework.stereotype.Service;

@Service
public class MLLanguageDetectionService implements LanguageDetectionService {

    private final LanguageDetectorME languageDetector;

    public MLLanguageDetectionService(LanguageDetectorModel languageDetectorModel) {
        this.languageDetector = new LanguageDetectorME(languageDetectorModel);
    }

    @Override
    public String predict(String language) {
        return this.languageDetector.predictLanguage(language).getLang();
    }
}
