package dev.jackraidenph.nliis.backend.configuration;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class AIConfiguration {

    @Bean
    public OllamaApi ollamaApi() {
        String url = System.getenv("OLLAMA_API_URL");
        if (url == null || url.isBlank()) {
            url = "http://localhost:11434";
        }
        return new OllamaApi(url);
    }

    @Bean
    @DependsOn("ollamaApi")
    public ChatModel chatModel(OllamaApi ollamaApi) {
        return new OllamaChatModel(
                ollamaApi,
                OllamaOptions.builder()
                        .withModel(OllamaModel.ORCA_MINI)
                        .build()
        );
    }

}
