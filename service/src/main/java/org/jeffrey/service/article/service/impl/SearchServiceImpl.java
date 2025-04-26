package org.jeffrey.service.article.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.es.ArticleEsDoc;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.article.repository.ArticleEsRepository;
import org.jeffrey.service.article.repository.entity.ArticleDO;
import org.jeffrey.service.article.repository.mapper.ArticleMapper;
import org.jeffrey.service.article.service.ArticleService;
import org.jeffrey.service.article.service.SearchService;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ArticleEsRepository articleEsRepository;
    private final ArticleMapper articleMapper;
    private final UserService userService;

    @Override
    public Page<ArticleEsDoc> search(String keyword, int page, int size) {
        try {
            // 构建布尔查询
            BoolQuery boolQuery = BoolQuery.of(b -> b
                    .should(MatchQuery.of(m -> m
                            .field("title")
                            .query(keyword)
                    )._toQuery())
                    .should(MatchQuery.of(m -> m
                            .field("content")
                            .query(keyword)
                    )._toQuery())
                    .should(MatchQuery.of(m -> m
                            .field("summary")
                            .query(keyword)
                    )._toQuery())
            );

            // 构建搜索请求
            SearchResponse<ArticleEsDoc> searchResponse = elasticsearchClient.search(s -> s
                            .index("article")
                            .query(q -> q.bool(boolQuery))
                            .from(page * size)
                            .size(size)
                            .source(src -> src.filter(f -> f.includes("id", "title", "summary", "content")))
                    , ArticleEsDoc.class);

            // 解析搜索结果
            List<ArticleEsDoc> articleList = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

            return new PageImpl<>(articleList, PageRequest.of(page, size), searchResponse.hits().total().value());

        } catch (IOException e) {
            e.printStackTrace(); // 打印真实的错误堆栈
            throw new RuntimeException("Elasticsearch query failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    @TraceLog("索引单篇文章")
    public String indexArticle(ArticleDO article) {
        try {
            if (article == null || article.getStatus() == 2) {
                // 不索引已删除的文章
                return null;
            }
            
            ArticleEsDoc esDoc = convertToEsDoc(article);
            ArticleEsDoc saved = articleEsRepository.save(esDoc);
            log.info("成功索引文章: id={}, title={}", article.getId(), article.getTitle());
            return saved.getId();
        } catch (Exception e) {
            log.error("索引文章失败: id={}, title={}", article.getId(), article.getTitle(), e);
            return null;
        }
    }
    
    @Override
    @TraceLog("批量索引文章")
    public int batchIndexArticles(List<ArticleDO> articles) {
        if (articles == null || articles.isEmpty()) {
            return 0;
        }
        
        try {
            List<ArticleEsDoc> esDocs = articles.stream()
                    .filter(article -> article.getStatus() != 2) // 不索引已删除的文章
                    .map(this::convertToEsDoc)
                    .collect(Collectors.toList());
            
            if (esDocs.isEmpty()) {
                return 0;
            }
            
            Iterable<ArticleEsDoc> saved = articleEsRepository.saveAll(esDocs);
            int count = 0;
            for (ArticleEsDoc doc : saved) {
                count++;
            }
            
            log.info("成功批量索引{}篇文章", count);
            return count;
        } catch (Exception e) {
            log.error("批量索引文章失败", e);
            return 0;
        }
    }
    
    @Override
    @TraceLog("同步所有文章到ES")
    public int syncAllArticles() {
        try {
            // 1. 查询所有已发布文章
            LambdaQueryWrapper<ArticleDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ArticleDO::getStatus, 1); // 只同步已发布的文章
            List<ArticleDO> articles = articleMapper.selectList(queryWrapper);
            
            if (articles.isEmpty()) {
                log.info("没有需要同步的文章");
                return 0;
            }
            
            // 2. 批量索引
            int count = batchIndexArticles(articles);
            log.info("成功同步{}篇文章到Elasticsearch", count);
            return count;
        } catch (Exception e) {
            log.error("同步文章到Elasticsearch失败", e);
            return 0;
        }
    }
    
    @Override
    @TraceLog("删除文章索引")
    public void deleteArticleIndex(Long articleId) {
        if (articleId == null) {
            return;
        }
        
        try {
            articleEsRepository.deleteById(articleId.toString());
            log.info("成功删除文章索引: id={}", articleId);
        } catch (Exception e) {
            log.error("删除文章索引失败: id={}", articleId, e);
        }
    }
    
    /**
     * 将ArticleDO转换为ArticleEsDoc
     */
    private ArticleEsDoc convertToEsDoc(ArticleDO article) {
        if (article == null) {
            return null;
        }
        
        ArticleEsDoc esDoc = new ArticleEsDoc();
        esDoc.setId(article.getId().toString()); // 使用文章ID作为ES文档ID
        esDoc.setTitle(article.getTitle());
        esDoc.setSummary(article.getSummary());
        esDoc.setContent(article.getContent());
        esDoc.setCreatedAt(article.getCreatedAt());
        esDoc.setStatus(article.getStatus());
        esDoc.setAuthorId(article.getAuthorId().toString());
        
        // 获取作者信息
        UserDO author = userService.getById(article.getAuthorId());
        if (author != null) {
            esDoc.setAuthorUsername(author.getUsername());
        }
        
        // TODO: 获取文章标签，这里先使用示例标签
        esDoc.setTags(List.of("Java", "Spring Boot", "Elasticsearch"));
        
        return esDoc;
    }
}
