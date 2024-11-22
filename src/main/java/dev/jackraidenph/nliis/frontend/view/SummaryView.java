package dev.jackraidenph.nliis.frontend.view;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.service.SummaryService;
import dev.jackraidenph.nliis.backend.service.SummaryService.Summary;
import dev.jackraidenph.nliis.backend.service.languagedetection.MLLanguageDetectionService;
import dev.jackraidenph.nliis.backend.utility.DocumentUtilities;
import dev.jackraidenph.nliis.frontend.utility.JavaFXCommonComponents;
import dev.jackraidenph.nliis.frontend.utility.JavaFXUtilities;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SummaryView implements View {

    private final SummaryService ruSummaryService;
    private final SummaryService itSummaryService;
    private final MLLanguageDetectionService mlLanguageDetectionService;
    private final FileChooser textDocumentFileChooser;
    private final Stage primaryStage;

    private Region rootRegion;

    private Region build() {

        ListView<Document> openDocuments = JavaFXCommonComponents.createGenericStringListView(Document::getTitle, false, false, false);

        Button openDocumentsButton = JavaFXCommonComponents.createGenericButton("Open", actionEvent -> {
            List<File> files = JavaFXUtilities.safeOpenFile(textDocumentFileChooser, primaryStage, true);
            List<Document> documents = DocumentUtilities.tryMapFilesToDocuments(files);
            openDocuments.getItems().clear();
            openDocuments.getItems().addAll(documents);
        });

        VBox results = new VBox();

        Button predictButton = JavaFXCommonComponents.createGenericButton("Create summary", actionEvent -> {
            results.getChildren().clear();
            for (Document document : openDocuments.getItems()) {

                SummaryService summaryService;
                String lang = this.mlLanguageDetectionService.predict(document.getContent());
                if (lang.equals("rus")) {
                    summaryService = ruSummaryService;
                } else if (lang.equals("ita")) {
                    summaryService = itSummaryService;
                } else {
                    throw new IllegalStateException("Unsupported summary language [%s].".formatted(lang));
                }

                Summary summary = summaryService.getDocumentSummary(document);

                HBox row = new HBox();
                row.setSpacing(10);
                row.setPadding(new Insets(10));

                row.getChildren().add(new Label(document.getTitle()));

                TextArea keywords = new TextArea();
                keywords.setWrapText(true);
                keywords.setEditable(false);
                keywords.setText(String.join("; ", summary.keywords()));
                row.getChildren().add(keywords);

                TextArea keySentences = new TextArea();
                keySentences.setWrapText(true);
                keySentences.setEditable(false);
                keySentences.setText(String.join("; ", summary.sentences()));
                row.getChildren().add(keySentences);

                Button open = new Button("Open Document");
                open.setOnAction(actionEvent1 -> {
                    DocumentUtilities.openDocument(document);
                });
                row.getChildren().add(open);

                results.getChildren().add(row);
            }
        });


        Node[] column = {openDocumentsButton, openDocuments, predictButton, results};
        Node[][] layout = {column};

        return JavaFXCommonComponents.createGenericColumnarLayout(layout, 10, 10);
    }

    @Override
    public Region getRootRegion() {
        if (this.rootRegion == null) {
            this.rootRegion = this.build();
        }
        return this.rootRegion;
    }
}
