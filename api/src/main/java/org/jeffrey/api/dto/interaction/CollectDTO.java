package org.jeffrey.api.dto.interaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectDTO implements Serializable {
    private Long articleId;
    private Long userId;
    private boolean isAdd; // true for collect, false for uncollect
} 