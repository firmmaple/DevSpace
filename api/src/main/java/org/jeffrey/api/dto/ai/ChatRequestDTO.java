package org.jeffrey.api.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    private String model;
    private List<ChatMessageDTO> messages;
    private Boolean stream;
    // Add other parameters like temperature, max_tokens if needed
} 