package dev.jackraidenph.nliis.backend.configuration;

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
}
