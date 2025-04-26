package org.jeffrey.service.article.repository;

import org.jeffrey.api.es.ArticleEsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch 搜索仓库接口
 */
@Repository
public interface ArticleEsRepository extends ElasticsearchRepository<ArticleEsDoc, String> {
    // 可以自定义扩展查询（比如 findByTitleContaining，或者用 QueryDSL）
}
