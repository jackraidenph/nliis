package dev.jackraidenph.nliis.backend.configuration;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaFXConfiguration {

    @Bean
    public FileChooser textDocumentFileChooser() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open Documents");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("PDF Files", "*.pdf")
        );

        return fileChooser;
    }

    @Bean
    public FileChooser mediaFileChooser() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open Documents");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("WAV Audio files", "*.wav")
        );

        return fileChooser;
    }

}
