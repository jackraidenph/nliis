package dev.jackraidenph.nliis.backend.data.document;

import dev.jackraidenph.nliis.backend.utility.StringUtilities;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public class PDFDocument extends Document {

    public PDFDocument(String title, Locale locale, Path pathToFile) {
        super(title, locale, pathToFile);
    }

    public PDFDocument(String title, String path, String locale) {
        super(title, path, locale);
    }

    public PDFDocument(String title, String path) {
        super(title, path);
    }

    @Override
    public String getContent() {
        try (PDDocument pdfDocument = Loader.loadPDF(this.getPathToFile().toFile())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return StringUtilities.normalizeForDocument(pdfTextStripper.getText(pdfDocument));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
