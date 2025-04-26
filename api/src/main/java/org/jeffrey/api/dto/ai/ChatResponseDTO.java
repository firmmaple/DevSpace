package org.jeffrey.api.dto.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatResponseDTO {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChoiceDTO> choices;
    // Include UsageDTO if needed

    // Helper to get the first choice's content easily
    public String getFirstChoiceContent() {
        if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }
} 