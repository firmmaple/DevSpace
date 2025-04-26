package org.jeffrey.api.dto.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChoiceDTO {
    private int index;
    private ResponseMessageDTO message;
    // Sambanova might include finish_reason, logprobs etc.
} 