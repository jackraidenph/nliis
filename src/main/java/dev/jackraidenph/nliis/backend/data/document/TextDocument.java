package dev.jackraidenph.nliis.backend.data.document;

import dev.jackraidenph.nliis.backend.utility.StringUtilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Collectors;

public class TextDocument extends Document {

    public TextDocument(String title, Locale locale, Path pathToFile) {
        super(title, locale, pathToFile);
    }

    public TextDocument(String title, String path, String locale) {
        super(title, path, locale);
    }

    public TextDocument(String title, String path) {
        super(title, path);
    }

    public BufferedReader getReader() {
        try {
            return Files.newBufferedReader(this.getPathToFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String message = "Failed to load a document from path [%s]: %s".formatted(this.getPathToFile(), e.getLocalizedMessage());
            throw new RuntimeException(message);
        }
    }

    @Override
    public String getContent() {
        return StringUtilities.normalizeForDocument(
                this.getReader()
                        .lines()
                        .map(l -> l.concat(System.lineSeparator()))
                        .collect(Collectors.joining())
        );
    }
}
