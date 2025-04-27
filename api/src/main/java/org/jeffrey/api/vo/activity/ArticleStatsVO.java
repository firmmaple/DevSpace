package org.jeffrey.api.vo.activity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 文章统计数据视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatsVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    
    private String title;
    
    // 总计数据
    private Integer totalViewCount;
    private Integer totalLikeCount;
    private Integer totalCollectCount;
    private Integer totalCommentCount;
    
    // 日趋势数据
    private List<String> dates; // 日期列表 ["2023-06-01", "2023-06-02", ...]
    private Map<String, List<Integer>> dailyStats; // {"views": [10, 20, ...], "likes": [5, 8, ...], ...}
    
    // 周同比数据
    private Double viewCountWeekOverWeek;  // 周同比变化率（百分比）
    private Double likeCountWeekOverWeek;
    private Double collectCountWeekOverWeek;
    
    // 访问来源数据 (待扩展)
    private Map<String, Integer> viewSources; // {"direct": 100, "search": 50, "referral": 30}
} 