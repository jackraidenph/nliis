package dev.jackraidenph.nliis.frontend.view;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.service.TranslationService;
import dev.jackraidenph.nliis.backend.utility.DocumentUtilities;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import dev.jackraidenph.nliis.frontend.utility.JavaFXCommonComponents;
import dev.jackraidenph.nliis.frontend.utility.JavaFXUtilities;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class TranslationView implements View {

    private final TranslationService translationService;
    private final SentenceModel enSentenceModel;
    private final ParserModel parserModel;
    private final FileChooser textDocumentFileChooser;
    private final Stage primaryStage;

    private Region rootRegion;

    private Region build() {

        this.translationService.loadFromCSV("dict.csv");

        ListView<Document> openDocuments = JavaFXCommonComponents.createGenericStringListView(Document::getTitle, false, false, false);

        Button openDocumentsButton = JavaFXCommonComponents.createGenericButton("Open", actionEvent -> {
            List<File> files = JavaFXUtilities.safeOpenFile(textDocumentFileChooser, primaryStage, false);
            List<Document> documents = DocumentUtilities.tryMapFilesToDocuments(files);
            openDocuments.getItems().clear();
            openDocuments.getItems().addAll(documents);
        });

        TextArea translateArea = new TextArea();
        translateArea.setEditable(false);
        translateArea.setWrapText(true);
        translateArea.setPromptText("Translated text here");

        ComboBox<String> sentences = new ComboBox<>();

        Label wordsBefore = new Label();
        Label wordsAfter = new Label();

        Button translateButton = JavaFXCommonComponents.createGenericButton("Translate Document", actionEvent -> {
            if (openDocuments.getItems().isEmpty()) {
                return;
            }

            Document document = openDocuments.getItems().getFirst();

            String original = document.getContent();

            translateArea.clear();
            String translated = this.translationService.translate(document);

            int wordsBeforeCount = StringUtilities.sanitize(original).split(StringUtilities.ONE_OR_MORE_SPACES).length;
            int wordsAfterCount = StringUtilities.sanitize(translated).split(StringUtilities.ONE_OR_MORE_SPACES).length;

            wordsBefore.setText("Before: %d".formatted(wordsBeforeCount));
            wordsAfter.setText("After: %d".formatted(wordsAfterCount));

            translateArea.setText(translated);
            sentences.getItems().clear();
            sentences.getItems().addAll(new SentenceDetectorME(enSentenceModel).sentDetect(original));
        });

        Label tree = new Label();

        Button showSentenceTree = JavaFXCommonComponents.createGenericButton("Show Parse Tree", actionEvent -> {
            StringBuffer stringBuffer = new StringBuffer();
            Parser parser = ParserFactory.create(this.parserModel);
            Parse[] parses = ParserTool.parseLine(sentences.getSelectionModel().getSelectedItem(), parser, 1);
            parses[0].show(stringBuffer);
            tree.setText(stringBuffer.toString());
        });

        Map<String, Function<Entry<String, String>, String>> columnFactories = new LinkedHashMap<>();
        columnFactories.put("From", Entry::getKey);
        columnFactories.put("To", Entry::getValue);
        TableView<Entry<String, String>> dictView = JavaFXCommonComponents.createGenericStringTableView(
                columnFactories,
                false,
                false,
                true
        );

        dictView.getItems().setAll(this.translationService.getVocabulary().entrySet());

        HBox add = new HBox();
        add.setSpacing(10);
        TextField from = new TextField();
        from.setPromptText("From");
        TextField to = new TextField();
        to.setPromptText("To");
        Button addButton = JavaFXCommonComponents.createGenericButton("Add", actionEvent -> {
            if (!from.getText().isBlank() && !to.getText().isBlank()) {
                this.translationService.addTranslation(from.getText(), to.getText());
                from.clear();
                to.clear();
                dictView.getItems().setAll(this.translationService.getVocabulary().entrySet());
            }
        });
        Button delete = JavaFXCommonComponents.createGenericButton("Delete", actionEvent -> {
            String key = dictView.getSelectionModel().getSelectedItem().getKey();
            this.translationService.removeTranslation(key);
            dictView.getItems().setAll(this.translationService.getVocabulary().entrySet());
        });
        add.getChildren().addAll(from, to, addButton, delete);


        Node[] column0 = {openDocumentsButton, openDocuments, translateButton, wordsBefore, wordsAfter, translateArea, sentences, showSentenceTree, tree};
        Node[] column1 = {dictView, add};
        Node[][] layout = {column0, column1};

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