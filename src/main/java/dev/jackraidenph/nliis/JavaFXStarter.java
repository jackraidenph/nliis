package dev.jackraidenph.nliis;

import dev.jackraidenph.nliis.ApplicationInit.StageReadyEvent;
import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.language.model.FrequentWordsModel;
import dev.jackraidenph.nliis.backend.data.language.model.ShortWordsModel;
import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.service.*;
import dev.jackraidenph.nliis.backend.service.languagedetection.FrequentWordsLanguageDetectionService;
import dev.jackraidenph.nliis.backend.service.languagedetection.MLLanguageDetectionService;
import dev.jackraidenph.nliis.backend.service.languagedetection.ShortWordsLanguageDetectionService;
import dev.jackraidenph.nliis.backend.utility.DocumentUtilities;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import dev.jackraidenph.nliis.frontend.view.CommonView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import opennlp.tools.sentdetect.SentenceModel;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JavaFXStarter implements ApplicationListener<StageReadyEvent> {

    private final CommonView commonView;

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        Stage stage = stageReadyEvent.getStage();

        Scene scene = new Scene(this.commonView.getRootRegion(), 640, 480);
        stage.setTitle("NLIIS");
        stage.setScene(scene);
        stage.show();
    }

    private static String unifiedLanguage(String lang) {
        return Locale.of(lang).getISO3Language();
    }
}