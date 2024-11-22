package dev.jackraidenph.nliis.backend.service.retrieval;

import dev.jackraidenph.nliis.backend.data.document.Document;

import java.util.List;

public interface RetrievalService {
    List<DocumentScore> retrieve(String query);


    record DocumentScore(Document document, double score) implements Comparable<DocumentScore> {
        @Override
        public int compareTo(DocumentScore o) {
            return (int) Math.signum(this.score() - o.score());
        }
    }
}
