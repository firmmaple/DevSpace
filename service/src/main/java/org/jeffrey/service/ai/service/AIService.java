package org.jeffrey.service.ai.service;

import org.jeffrey.api.dto.ai.ChatMessageDTO;
import org.jeffrey.api.exception.exception.AIConfigurationException;
import org.jeffrey.api.exception.exception.AIServiceException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface AIService {

    /**
     * Sends messages to the configured AI model and returns the response.
     *
     * @param messages List of chat messages to send to the AI model.
     * @return The AI model's response content.
     * @throws AIServiceException if API call fails
     * @throws AIConfigurationException if service is not properly configured
     */
    String chat(List<ChatMessageDTO> messages);
    
    /**
     * Convenience method that creates a single user message from a prompt.
     *
     * @param prompt The user's prompt.
     * @return The AI model's response content.
     * @throws AIServiceException if API call fails
     * @throws AIConfigurationException if service is not properly configured
     */
    String chat(String prompt);
    
    /**
     * Sends messages to the configured AI model and returns a streaming response.
     * Only available if streaming is enabled in configuration.
     *
     * @param messages List of chat messages to send to the AI model.
     * @return A StreamingResponseBody that can be used to stream the response to the client.
     * @throws AIServiceException if API call fails
     * @throws AIConfigurationException if service is not properly configured or streaming is disabled
     */
    StreamingResponseBody chatStream(List<ChatMessageDTO> messages);
    
    /**
     * Convenience method that creates a single user message from a prompt.
     *
     * @param prompt The user's prompt.
     * @return A StreamingResponseBody that can be used to stream the response to the client.
     * @throws AIServiceException if API call fails
     * @throws AIConfigurationException if service is not properly configured or streaming is disabled
     */
    StreamingResponseBody chatStream(String prompt);
    
    /**
     * Generates a summary of an article using the AI model.
     *
     * @param articleContent The content of the article to summarize.
     * @return A summary of the article.
     * @throws AIServiceException if API call fails
     * @throws AIConfigurationException if service is not properly configured
     * @throws IllegalArgumentException if article content is empty
     */
    String getArticleSummary(String articleContent);
    
    /**
     * Checks if streaming mode is enabled in the configuration.
     *
     * @return true if streaming is enabled, false otherwise.
     */
    boolean isStreamingEnabled();
} 