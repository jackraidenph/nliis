package dev.jackraidenph.nliis.backend.configuration;

import dev.jackraidenph.nliis.backend.service.SentenceDetectorService;
import dev.jackraidenph.nliis.backend.service.TranslationService;
import io.netty.handler.stream.ChunkedFile;
import lombok.extern.java.Log;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@Log

@Configuration
public class OpenNLPConfiguration {

    @Bean
    public SentenceModel ruSentenceModel() {
        Resource resource = new ClassPathResource("opennlp/models/sentencedetection/opennlp-ru-ud-gsd-sentence-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new SentenceModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load ru-RU sentence detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public SentenceModel itSentenceModel() {
        Resource resource = new ClassPathResource("opennlp/models/sentencedetection/opennlp-it-ud-vit-sentence-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new SentenceModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load it-IT sentence detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public SentenceModel enSentenceModel() {
        Resource resource = new ClassPathResource("opennlp/models/sentencedetection/opennlp-en-ud-ewt-sentence-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new SentenceModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load en-US sentence detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public POSModel ruPOSTaggingModel() {
        Resource resource = new ClassPathResource("opennlp/models/postagging/opennlp-ru-ud-gsd-pos-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new POSModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load ru-RU POS tagging model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public POSModel itPOSTaggingModel() {
        Resource resource = new ClassPathResource("opennlp/models/postagging/opennlp-it-ud-vit-pos-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new POSModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load it-IT POS tagging model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public POSModel enPOSTaggingModel() {
        Resource resource = new ClassPathResource("opennlp/models/postagging/opennlp-en-ud-ewt-pos-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new POSModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load en-US POS tagging model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public TokenizerModel ruTokenizerModel() {
        Resource resource = new ClassPathResource("opennlp/models/tokenizer/opennlp-ru-ud-gsd-tokens-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new TokenizerModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load ru-RU tokenizer detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public TokenizerModel itTokenizerModel() {
        Resource resource = new ClassPathResource("opennlp/models/tokenizer/opennlp-it-ud-vit-tokens-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new TokenizerModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load it-IT tokenizer detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public TokenizerModel enTokenizerModel() {
        Resource resource = new ClassPathResource("opennlp/models/tokenizer/opennlp-en-ud-ewt-tokens-1.1-2.4.0.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new TokenizerModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load en-US tokenizer detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public LemmatizerModel enLemmatizerModel() {
        Resource resource = new ClassPathResource("opennlp/models/lemmatizer/en-lemmatizer.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new LemmatizerModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load en lemmatizer detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ParserModel enParserModel() {
        Resource resource = new ClassPathResource("opennlp/models/parsing/en-parser-chunking.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new ParserModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load en parser detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    public LanguageDetectorModel languageDetectorModel() {
        Resource resource = new ClassPathResource("opennlp/models/langdetect-183.bin");
        try (InputStream is = new ByteArrayInputStream(resource.getContentAsByteArray())) {
            return new LanguageDetectorModel(is);
        } catch (IOException e) {
            log.severe("Couldn't load language detection model: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

}
