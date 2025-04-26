package org.jeffrey.api.dto.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseMessageDTO {
    private String role;
    private String content;
    // Sambanova might include other fields, add them if needed
} 