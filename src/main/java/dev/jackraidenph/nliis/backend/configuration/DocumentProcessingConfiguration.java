package dev.jackraidenph.nliis.backend.configuration;

import dev.jackraidenph.nliis.backend.repository.DocumentRepository;
import dev.jackraidenph.nliis.backend.repository.DocumentStatisticsRepository;
import dev.jackraidenph.nliis.backend.service.DocumentStatisticsService;
import dev.jackraidenph.nliis.backend.service.SentenceDetectorService;
import dev.jackraidenph.nliis.backend.service.SummaryService;
import dev.jackraidenph.nliis.backend.service.TranslationService;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import java.util.Locale;

@Configuration
public class DocumentProcessingConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DocumentRepository documentRepository() {
        return new DocumentRepository();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DocumentStatisticsRepository documentStatisticsRepository() {
        return new DocumentStatisticsRepository();
    }

    @Bean
    @DependsOn("ruSentenceModel")
    public SentenceDetectorService ruSentenceDetectorService(SentenceModel ruSentenceModel) {
        return new SentenceDetectorService(ruSentenceModel);
    }

    @Bean
    @DependsOn("itSentenceModel")
    public SentenceDetectorService itSentenceDetectorService(SentenceModel itSentenceModel) {
        return new SentenceDetectorService(itSentenceModel);
    }

    @Bean
    @DependsOn({"ruSentenceDetectorService", "documentStatisticsService"})
    public SummaryService ruSummaryService(SentenceDetectorService ruSentenceDetectorService, DocumentStatisticsService documentStatisticsService) {
        return new SummaryService(documentStatisticsService, ruSentenceDetectorService);
    }

    @Bean
    @DependsOn({"itSentenceDetectorService", "documentStatisticsService"})
    public SummaryService itSummaryService(SentenceDetectorService itSentenceDetectorService, DocumentStatisticsService documentStatisticsService) {
        return new SummaryService(documentStatisticsService, itSentenceDetectorService);
    }

    @Bean
    @DependsOn({"enLemmatizerModel", "enPOSTaggingModel", "enTokenizerModel", "enSentenceModel"})
    public TranslationService enRuTranslationService(
            LemmatizerModel enLemmatizerModel,
            POSModel enPOSTaggingModel,
            TokenizerModel enTokenizerModel,
            SentenceModel enSentenceModel
    ) {
        return new TranslationService(
                Locale.of("en-US"), Locale.of("ru-RU"),
                enLemmatizerModel,
                enPOSTaggingModel,
                enTokenizerModel,
                enSentenceModel
        );
    }

}
