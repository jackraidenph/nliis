package dev.jackraidenph.nliis.frontend.view;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import dev.jackraidenph.nliis.frontend.utility.JavaFXCommonComponents;
import dev.jackraidenph.nliis.frontend.utility.JavaFXUtilities;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.speech.synthesis.Synthesizer;
import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Component
public class SpeechView implements View {

    private final FileChooser mediaFileChooser;
    private final Stage primaryStage;
    private final Synthesizer synthesizer;
    private final Voice voice = VoiceManager.getInstance().getVoice("kevin");
    private final ChatModel chatModel;

    private Region rootRegion;

    private Region build() {

        if (voice != null) {
            voice.allocate();
        }

        MediaView mediaView = new MediaView();

        AtomicReference<File> file = new AtomicReference<>();

        Button openDocumentsButton = JavaFXCommonComponents.createGenericButton("Open", actionEvent -> {
            List<File> files = JavaFXUtilities.safeOpenFile(mediaFileChooser, primaryStage, false);
            if (!files.isEmpty()) {
                file.set(files.getFirst());
                mediaView.setMediaPlayer(new MediaPlayer(new Media(file.get().toPath().toUri().toString())));
            }
        });

        TextArea recognized = new TextArea();
        recognized.setPromptText("Recognized text");
        recognized.setWrapText(true);
        recognized.setEditable(false);

        TextArea answerField = new TextArea();
        recognized.setPromptText("Answer Field");
        recognized.setWrapText(true);
        recognized.setEditable(false);

        Label rateLabel = new Label("Speech Rate: ");
        TextField rateField = JavaFXCommonComponents.createGenericTextField("Speech Rate");
        rateField.setText("150");
        Label pitchLabel = new Label("Speech Pitch: ");
        TextField pitchField = JavaFXCommonComponents.createGenericTextField("Speech Pitch");
        pitchField.setText("100");
        Label volumeLabel = new Label("Speech Volume: ");
        TextField volumeField = JavaFXCommonComponents.createGenericTextField("Speech Volume");
        volumeField.setText("8");

        HBox rate = new HBox(rateLabel, rateField);
        HBox pitch = new HBox(pitchLabel, pitchField);
        HBox volume = new HBox(volumeLabel, volumeField);
        VBox settings = new VBox(rate, pitch, volume);
        settings.setSpacing(5);
        settings.setPadding(new Insets(5));

        Button recognizeButton = JavaFXCommonComponents.createGenericButton("Recognize", actionEvent -> {

            if (file.get() == null) {
                return;
            }

            Platform.runLater(() -> {
                String text = this.recognizeFile(file.get().getAbsolutePath());

                recognized.setText(text);

                answerField.setText(chatModel.call(text));

                int rateInt = Integer.parseInt(rateField.getText());
                int pitchInt = Integer.parseInt(pitchField.getText());
                int volumeInt = Integer.parseInt(volumeField.getText());

                this.speak(answerField.getText(), false, rateInt, pitchInt, volumeInt);
            });
        });

        TextField textField = new TextField();
        textField.setPromptText("Enter text to pronounce...");
        Button button = JavaFXCommonComponents.createGenericButton("Speak", actionEvent -> {
            int rateInt = Integer.parseInt(rateField.getText());
            int pitchInt = Integer.parseInt(pitchField.getText());
            int volumeInt = Integer.parseInt(volumeField.getText());
            this.speak(textField.getText(), true, rateInt, pitchInt, volumeInt);
        });


        Node[] column = {mediaView, openDocumentsButton, recognizeButton, recognized, answerField, textField, button};
        Node[] column1 = {settings};
        Node[][] layout = {column, column1};

        return JavaFXCommonComponents.createGenericColumnarLayout(layout, 10, 10);
    }

    private String recognizeFile(String file) {
        LibVosk.setLogLevel(LogLevel.DEBUG);

        try (Model model = new Model("recognizer/vosk-model-en-us-0.42-gigaspeech");
             InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
             Recognizer recognizer = new Recognizer(model, 8000)) {

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                recognizer.acceptWaveForm(b, nbytes);
            }

            String result = recognizer.getFinalResult();

            return result.substring(14, result.length() - 3).trim();
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }

    private void speak(String text, boolean save, int rate, int pitch, int volume) {
        Platform.runLater(() -> {
            try {
                if (this.voice != null) {

                    this.voice.setRate(rate);
                    this.voice.setPitch(pitch);
                    this.voice.setVolume(volume);
                    if (save) {
                        SingleFileAudioPlayer player = new SingleFileAudioPlayer(
                                "tts/" + StringUtilities.sanitize(text).replaceAll(StringUtilities.ONE_OR_MORE_SPACES, "_"),
                                Type.WAVE
                        );
                        this.voice.setAudioPlayer(player);
                        this.voice.speak(text);
                        player.close();
                        this.voice.setAudioPlayer(null);
                    }
                    this.voice.speak(text);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Region getRootRegion() {
        if (this.rootRegion == null) {
            this.rootRegion = this.build();
        }
        return this.rootRegion;
    }
}
