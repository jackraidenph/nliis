package dev.jackraidenph.nliis.frontend.view;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import dev.jackraidenph.nliis.frontend.utility.JavaFXCommonComponents;
import dev.jackraidenph.nliis.frontend.utility.JavaFXUtilities;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.WordResult;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.speech.synthesis.Synthesizer;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Component
public class SpeechView implements View {

    private final FileChooser mediaFileChooser;
    private final Stage primaryStage;
    private final Synthesizer synthesizer;
    private final Voice voice = VoiceManager.getInstance().getVoice("kevin");
    private final StreamSpeechRecognizer streamSpeechRecognizer;
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

        Button recognizeButton = JavaFXCommonComponents.createGenericButton("Recognize", actionEvent -> {

            if (file.get() == null) {
                return;
            }

            Stats stats = streamSpeechRecognizer.createStats(1);
            SpeechResult result;
            try (InputStream stream = new FileInputStream(file.get())) {
                streamSpeechRecognizer.startRecognition(stream);
                while ((result = streamSpeechRecognizer.getResult()) != null) {
                    stats.collect(result);
                }
                streamSpeechRecognizer.stopRecognition();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            StringJoiner joiner = new StringJoiner("\n");

            try (InputStream stream = new FileInputStream(file.get())) {
                // Transform represents the speech profile
                Transform transform = stats.createTransform();
                streamSpeechRecognizer.setTransform(transform);

                // Decode again with updated transform
                stream.skip(44);
                streamSpeechRecognizer.startRecognition(stream);
                while ((result = streamSpeechRecognizer.getResult()) != null) {
                    joiner.add(result.getHypothesis());
                }
                streamSpeechRecognizer.stopRecognition();
                recognized.setText(joiner.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String text = joiner.toString();

            recognized.setText(text);

            answerField.setText(chatModel.call(text));

            this.speak(answerField.getText(), false);
        });

        TextField textField = new TextField();
        textField.setPromptText("Enter text to pronounce...");
        Button button = JavaFXCommonComponents.createGenericButton("Speak", actionEvent -> this.speak(textField.getText(), true));


        Node[] column = {mediaView, openDocumentsButton, recognizeButton, recognized, answerField, textField, button};
        Node[][] layout = {column};

        return JavaFXCommonComponents.createGenericColumnarLayout(layout, 10, 10);
    }

    private void speak(String text, boolean save) {
        Platform.runLater(() -> {
            try {
                if (this.voice != null) {

                    this.voice.setRate(150);
                    this.voice.setPitch(155);
                    this.voice.setVolume(8);
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
