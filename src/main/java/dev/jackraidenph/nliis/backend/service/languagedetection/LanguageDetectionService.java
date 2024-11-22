package dev.jackraidenph.nliis.backend.service.languagedetection;

import java.util.Locale;

public interface LanguageDetectionService {

    String predict(String language);

}
