package dev.jackraidenph.nliis.frontend.view;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.service.languagedetection.FrequentWordsLanguageDetectionService;
import dev.jackraidenph.nliis.backend.service.languagedetection.MLLanguageDetectionService;
import dev.jackraidenph.nliis.backend.service.languagedetection.ShortWordsLanguageDetectionService;
import dev.jackraidenph.nliis.backend.utility.DocumentUtilities;
import dev.jackraidenph.nliis.frontend.utility.JavaFXCommonComponents;
import dev.jackraidenph.nliis.frontend.utility.JavaFXUtilities;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.function.Function;

@Component
public class LanguageDetectionView implements View {

    private final FrequentWordsLanguageDetectionService frequentWordsLanguageDetectionService;
    private final ShortWordsLanguageDetectionService shortWordsLanguageDetectionService;
    private final MLLanguageDetectionService mlLanguageDetectionService;
    private final FileChooser textDocumentFileChooser;
    private final Stage primaryStage;

    private Region rootRegion;

    public LanguageDetectionView(
            FrequentWordsLanguageDetectionService frequentWordsLanguageDetectionService,
            ShortWordsLanguageDetectionService shortWordsLanguageDetectionService,
            MLLanguageDetectionService mlLanguageDetectionService,
            FileChooser textDocumentFileChooser,
            Stage primaryStage
    ) {
        this.frequentWordsLanguageDetectionService = frequentWordsLanguageDetectionService;
        this.shortWordsLanguageDetectionService = shortWordsLanguageDetectionService;
        this.mlLanguageDetectionService = mlLanguageDetectionService;
        this.textDocumentFileChooser = textDocumentFileChooser;
        this.primaryStage = primaryStage;

        Document rus = DocumentUtilities.getDocumentFromFile(new File("deu_news_2021_10K-sentences.txt"));
        rus.setLocale(Locale.forLanguageTag("de-DE"));
        Document deu = DocumentUtilities.getDocumentFromFile(new File("rus_news_2021_10K-sentences.txt"));
        deu.setLocale(Locale.forLanguageTag("ru-RU"));
        this.frequentWordsLanguageDetectionService.fitModel(rus, deu);
        this.shortWordsLanguageDetectionService.fitModel(rus, deu);
    }

    private Region build() {

        ListView<Document> openDocuments = JavaFXCommonComponents.createGenericStringListView(Document::getTitle, false, false, false);

        Button openDocumentsButton = JavaFXCommonComponents.createGenericButton("Open", actionEvent -> {
            List<File> files = JavaFXUtilities.safeOpenFile(textDocumentFileChooser, primaryStage, true);
            List<Document> documents = DocumentUtilities.tryMapFilesToDocuments(files);
            openDocuments.getItems().clear();
            openDocuments.getItems().addAll(documents);
        });

        Map<String, Function<DocumentLanguagePrediction, String>> columnFactories = new LinkedHashMap<>();
        columnFactories.put("Title", pred -> pred.document().getTitle());
        columnFactories.put("Method", DocumentLanguagePrediction::method);
        columnFactories.put("Result", DocumentLanguagePrediction::result);
        columnFactories.put("Path to file", pred -> pred.document().getPathToFile().toString());
        TableView<DocumentLanguagePrediction> resultsTable = JavaFXCommonComponents.createGenericStringTableView(
                columnFactories,
                false,
                false,
                true,
                (tableRow, mouseEvent) ->
                        JavaFXUtilities.onDoubleClick(
                                mouseEvent,
                                doubleClick -> DocumentUtilities.openDocument(tableRow.getItem().document())
                        )
        );

        Button predictButton = JavaFXCommonComponents.createGenericButton("Predict", actionEvent -> {
            List<DocumentLanguagePrediction> predictions = new ArrayList<>();
            for (Document document : openDocuments.getItems()) {
                String fLang = this.frequentWordsLanguageDetectionService.predict(document.getContent());
                String sLang = this.shortWordsLanguageDetectionService.predict(document.getContent());
                String mLang = this.mlLanguageDetectionService.predict(document.getContent());
                predictions.add(new DocumentLanguagePrediction(document, "Frequent Words", tryParseLocale(fLang)));
                predictions.add(new DocumentLanguagePrediction(document, "Short Words", tryParseLocale(sLang)));
                predictions.add(new DocumentLanguagePrediction(document, "Machine Learning", tryParseLocale(mLang)));

                resultsTable.getItems().clear();
            }
            resultsTable.getItems().addAll(predictions);
        });


        Node[] column = {openDocumentsButton, openDocuments, predictButton, resultsTable};
        Node[][] layout = {column};

        return JavaFXCommonComponents.createGenericColumnarLayout(layout, 10, 10);
    }

    private static String tryParseLocale(String locale) {
        try {
            return Locale.of(Locale.of(locale).getISO3Language()).getDisplayLanguage();
        } catch (Exception e) {
            return locale;
        }
    }

    private record DocumentLanguagePrediction(Document document, String method, String result) {

    }

    @Override
    public Region getRootRegion() {
        if (this.rootRegion == null) {
            this.rootRegion = this.build();
        }
        return this.rootRegion;
    }
}
