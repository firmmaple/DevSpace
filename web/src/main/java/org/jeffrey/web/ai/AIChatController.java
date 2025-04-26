package org.jeffrey.web.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.ai.ChatMessageDTO;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.Status;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.api.exception.exception.AIConfigurationException;
import org.jeffrey.api.exception.exception.AIServiceException;
import org.jeffrey.service.ai.service.AIService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AIService aiService;

    /**
     * Simple endpoint to chat with the AI using just a prompt string.
     * Requires user to be logged in.
     *
     * @param payload Request body containing the "prompt".
     * @return AI response or error.
     */
    @TraceLog("AI Chat")
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResVo<String> chat(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "Prompt cannot be empty.");
        }

        try {
            String response = aiService.chat(prompt);
            return ResVo.ok(response);
        } catch (AIConfigurationException e) {
            // Catch configuration errors specifically
            log.error("AI Service configuration error: {}", e.getMessage());
            return ResVo.fail(new Status(503, "AI Service Unavailable: " + e.getMessage())); // Service Unavailable
        } catch (AIServiceException e) {
            log.error("AI Service error: {}", e.getMessage(), e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to get response from AI service: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error during AI chat processing for prompt: '{}'", prompt, e);
            // Return a generic server error to the user
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to get response from AI service.");
        }
    }

    /**
     * Advanced endpoint to chat with the AI using a list of messages.
     * Requires user to be logged in.
     *
     * @param messages List of chat messages with roles (system, user, assistant).
     * @return AI response or error.
     */
    @TraceLog("AI Chat Advanced")
    @PostMapping("/chat/advanced")
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResVo<String> chatAdvanced(@RequestBody List<ChatMessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "Messages list cannot be empty.");
        }

        try {
            String response = aiService.chat(messages);
            return ResVo.ok(response);
        } catch (AIConfigurationException e) {
            // Catch configuration errors specifically
            log.error("AI Service configuration error: {}", e.getMessage());
            return ResVo.fail(new Status(503, "AI Service Unavailable: " + e.getMessage())); // Service Unavailable
        } catch (AIServiceException e) {
            log.error("AI Service error: {}", e.getMessage(), e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to get response from AI service: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error during AI chat processing", e);
            // Return a generic server error to the user
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to get response from AI service.");
        }
    }

    /**
     * Endpoint to generate a summary of an article using AI.
     * Requires user to be logged in.
     *
     * @param payload Request body containing the article "content".
     * @return Summary of the article or error.
     */
    @TraceLog("AI Article Summary")
    @PostMapping("/summary")
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResVo<String> generateSummary(@RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "Article content cannot be empty.");
        }

        try {
            String summary = aiService.getArticleSummary(content);
            return ResVo.ok(summary);
        } catch (AIConfigurationException e) {
            // Catch configuration errors specifically
            log.error("AI Service configuration error: {}", e.getMessage());
            return ResVo.fail(new Status(503, "AI Service Unavailable: " + e.getMessage())); // Service Unavailable
        } catch (AIServiceException e) {
            log.error("AI Service error: {}", e.getMessage(), e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to generate summary: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid article content: {}", e.getMessage());
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, e.getMessage());
        } catch (Exception e) {
            log.error("Error generating article summary", e);
            // Return a generic server error to the user
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to generate summary from AI service.");
        }
    }

    /**
     * Streaming endpoint to chat with the AI using just a prompt string.
     * Requires user to be logged in.
     * Returns a streaming response with SSE format.
     *
     * @param payload Request body containing the "prompt".
     * @return Streaming response with the AI's response.
     */
    @TraceLog("AI Chat Stream")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResponseEntity<StreamingResponseBody> chatStream(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            if (!aiService.isStreamingEnabled()) {
                return ResponseEntity.status(503)
                    .body(outputStream -> outputStream.write(
                        "Streaming is not enabled on the server. Enable 'ai.sambanova.streamingEnabled' to use this feature."
                            .getBytes()));
            }
            
            StreamingResponseBody responseBody = aiService.chatStream(prompt);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(responseBody);
        } catch (AIConfigurationException e) {
            // Catch configuration errors specifically
            log.error("AI Service configuration error: {}", e.getMessage());
            return ResponseEntity.status(503)
                .body(outputStream -> outputStream.write(("AI Service Unavailable: " + e.getMessage()).getBytes()));
        } catch (Exception e) {
            log.error("Error during AI streaming chat for prompt: '{}'", prompt, e);
            return ResponseEntity.status(500)
                .body(outputStream -> outputStream.write(("Error: " + e.getMessage()).getBytes()));
        }
    }

    /**
     * Advanced streaming endpoint to chat with the AI using a list of messages.
     * Requires user to be logged in.
     * Returns a streaming response with SSE format.
     *
     * @param messages List of chat messages with roles (system, user, assistant).
     * @return Streaming response with the AI's response.
     */
    @TraceLog("AI Chat Stream Advanced")
    @PostMapping(value = "/chat/stream/advanced", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResponseEntity<StreamingResponseBody> chatStreamAdvanced(@RequestBody List<ChatMessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            if (!aiService.isStreamingEnabled()) {
                return ResponseEntity.status(503)
                    .body(outputStream -> outputStream.write(
                        "Streaming is not enabled on the server. Enable 'ai.sambanova.streamingEnabled' to use this feature."
                            .getBytes()));
            }
            
            StreamingResponseBody responseBody = aiService.chatStream(messages);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(responseBody);
        } catch (AIConfigurationException e) {
            // Catch configuration errors specifically
            log.error("AI Service configuration error: {}", e.getMessage());
            return ResponseEntity.status(503)
                .body(outputStream -> outputStream.write(("AI Service Unavailable: " + e.getMessage()).getBytes()));
        } catch (Exception e) {
            log.error("Error during AI streaming chat", e);
            return ResponseEntity.status(500)
                .body(outputStream -> outputStream.write(("Error: " + e.getMessage()).getBytes()));
        }
    }

    @TraceLog("AI Chat")
    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public ResVo<String> chat() {
        String prompt = "Hello World!";
        try {
            String response = aiService.chat(prompt);
            return ResVo.ok(response);
        } catch (AIConfigurationException e) {
            // Catch configuration errors specifically
            log.error("AI Service configuration error: {}", e.getMessage());
            return ResVo.fail(new Status(503, "AI Service Unavailable: " + e.getMessage())); // Service Unavailable
        } catch (AIServiceException e) {
            log.error("AI Service error: {}", e.getMessage(), e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to get response from AI service: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error during AI chat processing for prompt: '{}'", prompt, e);
            // Return a generic server error to the user
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, "Failed to get response from AI service.");
        }
    }
} 