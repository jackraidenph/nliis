package dev.jackraidenph.nliis.backend.repository;

import dev.jackraidenph.nliis.backend.data.document.Document;

import java.util.*;

public class DocumentRepository {

    private final List<Document> documents = new ArrayList<>();

    public DocumentRepository(Document... documents) {
        this.documents.addAll(List.of(documents));
    }

    public void addDocument(Document document) {
        if (this.documents.contains(document)) {
            return;
        }
        this.documents.add(document);
    }

    public void addDocuments(Iterable<Document> documents) {
        for (Document document : documents) {
            this.addDocument(document);
        }
    }

    public void addDocuments(Document... documents) {
        this.addDocuments(Arrays.asList(documents));
    }

    public List<Document> getDocuments() {
        return new ArrayList<>(documents);
    }

    public void clear() {
        this.documents.clear();
    }

    public int getTotalDocuments() {
        return this.documents.size();
    }

}
