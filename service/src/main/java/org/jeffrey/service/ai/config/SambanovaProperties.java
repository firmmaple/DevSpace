package org.jeffrey.service.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.sambanova")
public class SambanovaProperties {

    /**
     * Sambanova API Key. Should be set via environment variable SAMBANOVA_API_KEY.
     */
    private String apiKey;

    /**
     * Base URL for the Sambanova API.
     */
    private String baseUrl = "https://api.sambanova.ai/v1/";

    /**
     * Default AI model to use for chat completions.
     */
    private String model = "Meta-Llama-3.1-405B-Instruct";
    
    /**
     * Whether to use streaming mode for API responses.
     */
    private boolean streamingEnabled = false;
} 