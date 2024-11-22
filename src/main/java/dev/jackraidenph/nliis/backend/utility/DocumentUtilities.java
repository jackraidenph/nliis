package dev.jackraidenph.nliis.backend.utility;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.PDFDocument;
import dev.jackraidenph.nliis.backend.data.document.TextDocument;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class DocumentUtilities {

    public static Document getDocumentFromFile(File file) {
        String docTitle = file.getName().replaceAll("\\.(.+)$", "");
        String extension = file.getName().replace(docTitle, "").substring(1);
        Document document;

        document = switch (extension) {
            case "txt" -> new TextDocument(docTitle, file.toPath().toString());
            case "pdf" -> new PDFDocument(docTitle, file.toPath().toString());
            default -> throw new RuntimeException("Extension [%s] not supported!".formatted(extension));
        };

        return document;
    }

    public static void openDocument(Document document) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(document.getPathToFile().toFile());
            } catch (IOException ioException) {
                throw new RuntimeException("Couldn't open a document due to an IOException.");
            }
        } else {
            throw new IllegalStateException("Desktop is not supported.");
        }
    }

    public static List<Document> tryMapFilesToDocuments(List<File> files) {
        return files.stream()
                .map(f -> {
                    try {
                        return DocumentUtilities.getDocumentFromFile(f);
                    } catch (RuntimeException extensionException) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
