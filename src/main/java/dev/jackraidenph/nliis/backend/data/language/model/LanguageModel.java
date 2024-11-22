package dev.jackraidenph.nliis.backend.data.language.model;

import dev.jackraidenph.nliis.backend.data.document.Document;

public interface LanguageModel {

    String UNCLASSIFIED = "<UNCLASSIFIED>";

    void fit(Document... documents);

    String classify(String textToEvaluate);
}
