package dev.jackraidenph.nliis.frontend.view;

import dev.jackraidenph.nliis.backend.controller.RetrievalController;
import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.DocumentStatistics;
import dev.jackraidenph.nliis.backend.service.retrieval.RetrievalService.DocumentScore;
import dev.jackraidenph.nliis.backend.utility.DocumentUtilities;
import dev.jackraidenph.nliis.frontend.utility.JavaFXCommonComponents;
import dev.jackraidenph.nliis.frontend.utility.JavaFXUtilities;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RetrievalView implements View {

    private final Stage primaryStage;
    private final FileChooser textDocumentFileChooser;
    private final RetrievalController controller;
    private final ChatModel chatModel;

    private Region rootRegion;

    @Override
    public Region getRootRegion() {
        if (this.rootRegion == null) {
            this.rootRegion = this.build();
        }
        return this.rootRegion;
    }

    private String getRelevancyJustification(String query, String text) {
        Prompt prompt = new Prompt("Answer if the text is relevant to the query or not. The query searches for specific words. Exactly one simple sentence. Query: %s. Text: %s".formatted(query, text));
        return this.chatModel.call(prompt).getResult().getOutput().getContent();
    }

    private Region build() {

        TextField queryField = JavaFXCommonComponents.createGenericTextField("Enter query...");

        Map<String, Function<DocumentRow, String>> columnFactories = new LinkedHashMap<>();
        columnFactories.put("Title", ds -> ds.documentScore().document().getTitle());
        columnFactories.put("Path", ds -> ds.documentScore().document().getPathToFile().toString());
        columnFactories.put("Search Score", ds -> "%.3f".formatted(ds.documentScore().score()));
        columnFactories.put("Justification", DocumentRow::justification);
        TableView<DocumentRow> resultsTable = JavaFXCommonComponents.createGenericStringTableView(
                columnFactories,
                false,
                false,
                true,
                (tableRow, mouseEvent) ->
                        JavaFXUtilities.onDoubleClick(
                                mouseEvent,
                                doubleClick -> DocumentUtilities.openDocument(tableRow.getItem().documentScore().document())
                        )
        );

        Button searchButton = JavaFXCommonComponents.createGenericButton("Search", actionEvent -> {
            resultsTable.getItems().clear();
            List<DocumentScore> retrieved = this.controller.retrieveDocuments(queryField.getText());
            List<DocumentRow> documentRows = new ArrayList<>();
            for (DocumentScore documentScore : retrieved) {
                documentRows.add(new DocumentRow(documentScore, this.getRelevancyJustification(queryField.getText(), documentScore.document().getContent())));
            }
            resultsTable.getItems().addAll(documentRows);
        });

        Node[] leftColumn = {queryField, searchButton, resultsTable};

        ListView<Document> openedDocumentsList = JavaFXCommonComponents.createGenericStringListView(
                Document::getTitle,
                true
        );

        Button openDocumentsButton = JavaFXCommonComponents.createGenericButton(
                "Open",
                actionEvent -> {
                    List<File> selectedFiles = JavaFXUtilities.safeOpenFile(textDocumentFileChooser, primaryStage, true);
                    this.controller.clearRepository();
                    openedDocumentsList.getItems().clear();
                    List<Document> documents = DocumentUtilities.tryMapFilesToDocuments(selectedFiles);
                    this.controller.addDocumentsToRepository(documents);
                    openedDocumentsList.getItems().addAll(documents);
                }
        );

        ListView<Document> processedDocumentsList = JavaFXCommonComponents.createGenericStringListView(
                Document::getTitle,
                false,
                false,
                false
        );

        Button processDocumentsButton = JavaFXCommonComponents.createGenericButton(
                "Process",
                actionEvent -> {
                    List<Document> chosenDocuments = openedDocumentsList.getSelectionModel().getSelectedItems();
                    Map<Document, DocumentStatistics> processed = this.controller.processDocuments(chosenDocuments);
                    processedDocumentsList.getItems().addAll(processed.keySet());
                }
        );

        Node[] rightColumn = {openDocumentsButton, openedDocumentsList, processDocumentsButton, processedDocumentsList};

        Node[][] layout = {leftColumn, rightColumn};

        return JavaFXCommonComponents.createGenericColumnarLayout(layout, 10, 10);
    }

    private record DocumentRow(DocumentScore documentScore, String justification) {

    }
}
