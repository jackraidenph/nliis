package dev.jackraidenph.nliis.backend.configuration;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.springframework.context.annotation.Bean;

import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import java.io.IOException;
import java.util.Locale;

@org.springframework.context.annotation.Configuration
public class SpeechConfiguration {

    @Bean
    public Synthesizer speechSynthesizer() throws EngineException {
        return Central.createSynthesizer(new SynthesizerModeDesc(Locale.US));
    }

    @Bean
    public StreamSpeechRecognizer streamSpeechRecognizer() throws IOException {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        configuration.setSampleRate(8000);
        return new StreamSpeechRecognizer(configuration);
    }
}