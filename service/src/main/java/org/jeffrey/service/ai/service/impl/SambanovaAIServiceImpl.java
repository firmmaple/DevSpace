package org.jeffrey.service.ai.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.ai.ChatMessageDTO;
import org.jeffrey.api.dto.ai.ChatRequestDTO;
import org.jeffrey.api.dto.ai.ChatResponseDTO;
import org.jeffrey.service.ai.config.SambanovaProperties;
import org.jeffrey.api.exception.exception.AIConfigurationException;
import org.jeffrey.api.exception.exception.AIServiceException;
import org.jeffrey.service.ai.service.AIService;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SambanovaAIServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final SambanovaProperties sambanovaProperties;

    // Inject API key from environment variable
    @Value("${ai.sambanova.apiKey}") // Use : to provide an empty default if not set
    private String apiKey;

    private static final String SYSTEM_SUMMARY_PROMPT =
        "You are an AI assistant tasked with generating concise, informative summaries of articles. " +
        "Generate a summary that captures the main points of the article in 1-2 sentences. " +
        "The summary should be clear, objective, and highlight the most important information. " +
        "Do not include your own opinions or comments about the article. " +
        "Focus only on what is contained in the original text. " +
        "The summary must use the same language as the original article.";


    @Override
    public boolean isStreamingEnabled() {
        return sambanovaProperties.isStreamingEnabled();
    }

    @Override
    public String chat(String prompt) {
        ChatMessageDTO userMessage = new ChatMessageDTO("user", prompt);
        return chat(Collections.singletonList(userMessage));
    }

    @Override
    public String chat(List<ChatMessageDTO> messages) {
        validateConfiguration();
        validateMessages(messages);

        String apiUrl = sambanovaProperties.getBaseUrl() + "chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ChatRequestDTO requestPayload = new ChatRequestDTO(
                sambanovaProperties.getModel(),
                messages,
                false // Always use non-streaming for this method
        );

        HttpEntity<ChatRequestDTO> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            log.info("Sending chat request to Sambanova API: URL={}, Model={}", apiUrl, sambanovaProperties.getModel());
            ResponseEntity<ChatResponseDTO> responseEntity = restTemplate.postForEntity(
                    apiUrl,
                    requestEntity,
                    ChatResponseDTO.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                String responseContent = responseEntity.getBody().getFirstChoiceContent();
                if (responseContent != null) {
                    log.info("Received successful response from Sambanova API.");
                    return responseContent;
                } else {
                    log.error("Sambanova API response did not contain expected content. Body: {}", responseEntity.getBody());
                    throw new AIServiceException("AI service returned an unexpected response format.");
                }
            } else {
                log.error("Sambanova API call failed with status: {}. Body: {}", responseEntity.getStatusCode(), responseEntity.getBody());
                throw new AIServiceException("AI service request failed with status code: " + responseEntity.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error calling Sambanova API: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AIServiceException("Error interacting with AI service: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof AIServiceException) {
                throw e;
            }
            log.error("An unexpected error occurred during AI service call.", e);
            throw new AIServiceException("An unexpected error occurred while contacting the AI service.", e);
        }
    }

    @Override
    public StreamingResponseBody chatStream(String prompt) {
        ChatMessageDTO userMessage = new ChatMessageDTO("user", prompt);
        return chatStream(Collections.singletonList(userMessage));
    }

    @Override
    public StreamingResponseBody chatStream(List<ChatMessageDTO> messages) {
        if (!isStreamingEnabled()) {
            throw new AIConfigurationException("Streaming is not enabled in the configuration. Enable 'ai.sambanova.streamingEnabled' to use this feature.");
        }
        
        validateConfiguration();
        validateMessages(messages);

        String apiUrl = sambanovaProperties.getBaseUrl() + "chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.setAccept(Collections.singletonList(MediaType.TEXT_EVENT_STREAM));

        ChatRequestDTO requestPayload = new ChatRequestDTO(
                sambanovaProperties.getModel(),
                messages,
                true // Use streaming
        );

        // Get JSON converter
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        
        log.info("Sending streaming chat request to Sambanova API: URL={}, Model={}", apiUrl, sambanovaProperties.getModel());
        
        // Create final references for closure
        final ChatRequestDTO finalRequest = requestPayload;

        return outputStream -> {
            try {
                // Define request callback to write our request payload
                RequestCallback requestCallback = request -> {
                    request.getHeaders().putAll(headers);
                    jsonConverter.write(finalRequest, MediaType.APPLICATION_JSON, request);
                };

                // Define response extractor to handle the streaming response
                ResponseExtractor<Void> responseExtractor = response -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                                // Extract content from SSE data line
                                String content = line.substring(6); // Remove "data: " prefix
                                outputStream.write(content.getBytes());
                                outputStream.flush();
                            }
                        }
                    } catch (IOException e) {
                        outputStream.write(("Error reading stream: " + e.getMessage()).getBytes());
                    }
                    return null;
                };

                // Execute the request with proper streaming handling
                restTemplate.execute(apiUrl, HttpMethod.POST, requestCallback, responseExtractor);
            } catch (Exception e) {
                log.error("Error during streaming response", e);
                try {
                    outputStream.write(("Error during streaming: " + e.getMessage()).getBytes());
                } catch (IOException ex) {
                    // Ignore write errors during error handling
                }
            }
        };
    }

    @Override
    public String getArticleSummary(String articleContent) {
        if (articleContent == null || articleContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Article content cannot be empty");
        }

        List<ChatMessageDTO> messages = Arrays.asList(
            new ChatMessageDTO("system", SYSTEM_SUMMARY_PROMPT),
            new ChatMessageDTO("user", articleContent)
        );

        return chat(messages);
    }
    
    /**
     * Validates the configuration for the AI service.
     * 
     * @throws AIConfigurationException if any required configuration is missing.
     */
    private void validateConfiguration() throws AIConfigurationException {
        if (!StringUtils.hasText(apiKey)) {
            log.error("Sambanova API key (SAMBANOVA_API_KEY) is not configured.");
            throw new AIConfigurationException("AI Service is not configured: Missing API Key.");
        }
        if (!StringUtils.hasText(sambanovaProperties.getBaseUrl())) {
            log.error("Sambanova Base URL is not configured.");
            throw new AIConfigurationException("AI Service is not configured: Missing Base URL.");
        }
        if (!StringUtils.hasText(sambanovaProperties.getModel())) {
            log.error("Sambanova Model is not configured.");
            throw new AIConfigurationException("AI Service is not configured: Missing Model.");
        }
    }

    /**
     * Validates the messages for the AI service.
     * 
     * @param messages The messages to validate.
     * @throws IllegalArgumentException if the messages list is null or empty.
     */
    private void validateMessages(List<ChatMessageDTO> messages) throws IllegalArgumentException {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages list cannot be null or empty");
        }
    }
} 