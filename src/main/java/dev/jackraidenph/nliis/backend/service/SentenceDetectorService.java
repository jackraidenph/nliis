package dev.jackraidenph.nliis.backend.service;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import java.util.ArrayList;
import java.util.List;

public class SentenceDetectorService {

    private final SentenceDetectorME sentenceDetector;

    public SentenceDetectorService(SentenceModel sentenceModel) {
        this.sentenceDetector = new SentenceDetectorME(sentenceModel);
    }

    public List<Sentence> splitIntoSentences(String text) {
        String[] sentences = this.sentenceDetector.sentDetect(text);
        Span[] spans = this.sentenceDetector.sentPosDetect(text);
        List<Sentence> sentenceList = new ArrayList<>();
        for (int i = 0; i < sentences.length; i++) {
            sentenceList.add(new Sentence(sentences[i], spans[i]));
        }
        return sentenceList;
    }

    public record Sentence(String text, Span span) {

    }
}
