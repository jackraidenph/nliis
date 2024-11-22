package dev.jackraidenph.nliis.backend.service;

import dev.jackraidenph.nliis.backend.data.document.Document;
import dev.jackraidenph.nliis.backend.data.document.TextDocument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TranslationService {

    private final Map<String, String> vocabulary = new HashMap<>();
    @Getter
    private final Locale fromLanguage, toLanguage;

    private final TokenizerME tokenizer;
    private final LemmatizerME lemmatizer;
    private final POSTaggerME posTagger;
    private final SentenceDetectorME sentenceDetector;

    public TranslationService(
            Locale from, Locale to,
            LemmatizerModel lemmatizerModel,
            POSModel posModel,
            TokenizerModel tokenizerModel,
            SentenceModel sentenceModel
    ) {
        this.fromLanguage = from;
        this.toLanguage = to;
        this.lemmatizer = new LemmatizerME(lemmatizerModel);
        this.posTagger = new POSTaggerME(posModel);
        this.tokenizer = new TokenizerME(tokenizerModel);
        this.sentenceDetector = new SentenceDetectorME(sentenceModel);
    }

    public Map<String, String> getVocabulary() {
        return Collections.unmodifiableMap(this.vocabulary);
    }

    public void clearTranslations() {
        this.vocabulary.clear();
    }

    public void addTranslation(String original, String translation) {
        String from = original.strip();
        String[] token = {from};
        String[] pos = posTagger.tag(token);
        String[] lemma = lemmatizer.lemmatize(token, pos);
        this.vocabulary.put(lemma[0], translation);
    }

    public void removeTranslation(String original) {
        this.vocabulary.remove(original);
    }

    public String translate(Document document) {
        String[] sentences = this.sentenceDetector.sentDetect(document.getContent());
        StringJoiner stringJoiner = new StringJoiner(" ");
        for (String s : sentences) {
            stringJoiner.add(translate(s).strip());
        }
        return stringJoiner.toString();
    }

    private String translate(String original) {
        String[] tokens = this.tokenizer.tokenize(original);
        String[] posTags = this.posTagger.tag(tokens);
        String[] lemmas = this.lemmatizer.lemmatize(tokens, posTags);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lemmas.length; i++) {
            if (!Objects.equals(posTags[i], "PUNCT")) {
                String translation = this.vocabulary.get(lemmas[i]);
                if (Character.isUpperCase(tokens[i].charAt(0))) {
                    translation = StringUtils.capitalize(translation);
                }
                if (translation != null) {
                    builder.append(' ').append(translation);
                }
            } else {
                builder.append(lemmas[i]);
            }
        }

        return builder.toString().strip();
    }

    public void loadFromCSV(String pathToFile) {
        this.vocabulary.clear();
        try (Stream<String> lines = Files.lines(Path.of(pathToFile))) {
            lines
                    .skip(1)
                    .map(l -> l.split(","))
                    .filter(a -> a.length == 2)
                    .forEach(values -> {
                        String translation = values[1].strip();
                        this.addTranslation(values[0], translation);
                    });
        } catch (IOException ioException) {
            throw new RuntimeException();
        }
    }

}
