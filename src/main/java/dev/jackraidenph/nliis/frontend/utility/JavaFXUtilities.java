package dev.jackraidenph.nliis.frontend.utility;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JavaFXUtilities {

    public static void onDoubleClick(MouseEvent consumed, Consumer<MouseEvent> wrappedConsumer) {
        if (consumed.getButton() == MouseButton.PRIMARY && consumed.getClickCount() == 2) {
            wrappedConsumer.accept(consumed);
        }
    }

    public static List<File> safeOpenFile(FileChooser fileChooser, Window window, boolean multiple) {
        List<File> files = new ArrayList<>();

        if (fileChooser == null || window == null) {
            return files;
        }

        if (multiple) {
            List<File> chosen = fileChooser.showOpenMultipleDialog(window);
            if (chosen != null) {
                files.addAll(chosen);
            }
        } else {
            File chosen = fileChooser.showOpenDialog(window);
            if (chosen != null) {
                files.add(chosen);
            }
        }

        return files;
    }

}
