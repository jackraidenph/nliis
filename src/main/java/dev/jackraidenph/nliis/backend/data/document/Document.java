package dev.jackraidenph.nliis.backend.data.document;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

@Data
public abstract class Document {

    private final Instant created = Instant.now();

    public Document(String title, Locale locale, Path pathToFile) {
        this.title = title;
        this.pathToFile = pathToFile;
        this.locale = locale;
    }

    public Document(String title, String path, String locale) {
        this.title = title;
        this.pathToFile = Path.of(path);
        this.locale = Locale.forLanguageTag(locale);
    }

    public Document(String title, String path) {
        this.title = title;
        this.pathToFile = Path.of(path);
        this.locale = null;
    }

    private String title;
    private Locale locale;
    private Path pathToFile;
    private long length = -1;
    @Getter(AccessLevel.PRIVATE)
    private boolean dirty = true;

    public long getLength() {
        if (this.length < 0 || this.dirty) {
            this.length = this.getContent().length();
            this.dirty = false;
        }
        return this.length;
    }

    public void setPathToFile(Path pathToFile) {
        this.pathToFile = pathToFile;
        this.dirty = true;
    }

    public String getContent() {
        return null;
    }
}
