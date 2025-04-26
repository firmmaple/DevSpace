package org.jeffrey.web.article;

import lombok.RequiredArgsConstructor;
import org.jeffrey.api.es.ArticleEsDoc;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.service.article.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article/search")
@RequiredArgsConstructor
public class SearchRestController {

    private final SearchService SearchService;

    @GetMapping
    public ResVo<Page<ArticleEsDoc>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ArticleEsDoc> result = SearchService.search(keyword, page, size);
        return ResVo.ok(result);
    }
}
