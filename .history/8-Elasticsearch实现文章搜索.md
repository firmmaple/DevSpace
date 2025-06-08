## Elasticsearch实现文章搜索

DevSpace 集成了 Elasticsearch 实现高效的文章全文搜索功能，支持对标题、内容和摘要的多字段搜索。

### 已实现功能

1. **全文搜索**:
   * 支持对文章标题、内容和摘要的多字段搜索
   * 使用中文分词器（IK分词器）提高中文搜索质量
   * 分页获取搜索结果

2. **索引管理**:
   * 自动索引新发布的文章
   * 在文章更新时自动更新索引
   * 在文章删除时自动删除索引
   * 管理员可通过后台手动同步所有文章到 Elasticsearch

3. **搜索接口**:
   * 提供 `/api/article/search` REST API 端点
   * 支持关键词搜索和分页参数

### 技术实现

1. **服务层**:
   * `SearchService` 接口定义搜索相关操作
   * `SearchServiceImpl` 实现搜索逻辑和索引管理
   * `ArticleEsRepository` 提供基础的 Elasticsearch 访问能力

2. **索引管理**:
   * 索引自动同步: 在文章的创建、更新和删除操作中触发相应的索引操作
   * 同步机制: `ArticleServiceImpl` 中的各方法内调用 `searchService` 进行索引维护

3. **前端实现**:
   * 文章列表页集成搜索表单
   * 使用正则表达式在前端实现关键词高亮显示
   * 搜索状态保存在 URL 参数中，支持分享搜索结果

4. **管理功能**:
   * 在管理后台提供 Elasticsearch 管理页面
   * 支持手动触发全量索引同步

### 技术亮点

1. **多字段搜索**:
   * 使用布尔查询组合多个字段匹配，提高搜索相关性
   * 针对标题、内容、摘要分别设置权重，优化搜索结果排序

2. **错误处理**:
   * 搜索服务的错误不影响核心功能
   * 日志记录详细的错误信息，便于排查

3. **自动化集成**:
   * 文章生命周期事件自动触发相应的索引操作
   * 无需额外维护，确保搜索结果与数据库保持同步

### 后续计划

1. 实现基于标签的过滤搜索
2. 实现更多高级搜索功能（如时间范围过滤、作者过滤等）
3. 优化搜索结果相关性算法

### 搜索接口 (`/api/article/search`)

    - `GET /api/article/search`: 全文搜索文章
        - Parameters:
            - `keyword` (String): 搜索关键词
            - `page` (int, default: 0): 页码（Elasticsearch分页从0开始）
            - `size` (int, default: 10): 每页条目数
        - Response: `ResVo<Page<ArticleEsDoc>>` (Spring Data 分页对象)
        - **Authentication**: Not Required (无需登录)
        - **Description**: 搜索关键词匹配文章标题、内容和摘要，使用布尔查询并支持中文分词



### Elasticsearch设计

#### 概述

DevSpace使用Elasticsearch实现全文搜索功能，允许用户搜索文章的标题、内容和摘要。

#### 索引结构

文章索引使用IK分词器（`ik_max_word`/`ik_smart`）对中文内容进行分词，优化搜索结果相关性。

#### `article`索引

主要字段：

```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "title": { 
        "type": "text", 
        "analyzer": "ik_max_word", 
        "search_analyzer": "ik_smart" 
      },
      "summary": { 
        "type": "text", 
        "analyzer": "ik_max_word", 
        "search_analyzer": "ik_smart" 
      },
      "content": { 
        "type": "text", 
        "analyzer": "ik_max_word", 
        "search_analyzer": "ik_smart" 
      },
      "tags": { "type": "keyword" },
      "authorId": { "type": "keyword" },
      "authorUsername": { "type": "keyword" },
      "createdAt": { "type": "date" },
      "status": { "type": "integer" }
    }
  }
}
```

#### 搜索策略

##### - 多字段布尔查询

使用布尔查询（Bool Query）组合多个字段的匹配查询（Match Query），提高搜索结果的相关性。查询示例：

```json
{
  "bool": {
    "should": [
      { "match": { "title": "搜索关键词" } },
      { "match": { "content": "搜索关键词" } },
      { "match": { "summary": "搜索关键词" } }
    ]
  }
}
```

##### - 分词策略

- **索引时**：使用 `ik_max_word` 进行最细粒度分词，提高召回率
- **搜索时**：使用 `ik_smart` 进行智能分词，提高搜索精度

#### 索引同步机制

##### 自动触发

文章的创建、更新、删除操作会自动触发相应的Elasticsearch索引操作：

- 文章创建（status=1）：自动索引到Elasticsearch
- 文章更新：如果状态为已发布，更新索引；如果状态变为草稿，删除索引
- 文章删除：从Elasticsearch中删除索引

##### 手动同步

管理员可以通过管理后台的"Elasticsearch管理"页面手动触发全量索引同步：

- 触发方式：通过 `/admin/elasticsearch/sync-articles` 接口
- 实现：`syncAllArticles` 方法扫描所有已发布文章并批量索引

#### 错误处理

所有Elasticsearch操作都有适当的错误处理机制，确保即使索引操作失败也不影响核心业务流程：

- 索引错误：记录错误日志，但不中断文章CRUD操作
- 搜索错误：向用户返回友好的错误信息，并记录详细日志用于排查

#### 关键词高亮

已在前端实现搜索关键词高亮功能：

1. **实现方式**：使用JavaScript在客户端对搜索结果中的标题和摘要进行处理，使用正则表达式识别关键词并添加高亮样式
2. **实现位置**：`ui/src/main/resources/static/js/article/list.js` 中的 `highlightKeyword()` 函数
3. **高亮样式**：给匹配的关键词添加 `text-danger` 类（Bootstrap的红色文本样式）
4. **错误处理**：包含正则表达式异常处理，确保特殊字符不会导致程序崩溃

代码实现：
```javascript
function highlightKeyword(element, text, keyword) {
    if (!keyword || !text) return;
    
    try {
        const regex = new RegExp(`(${keyword})`, 'gi');
        element.innerHTML = text.replace(regex, '<span class="text-danger">$1</span>');
    } catch (e) {
        // 如果关键词包含特殊字符，正则可能会出错，此时保持原样
        element.textContent = text;
    }
}
```

#### 后续优化计划

1. **加权搜索**：为标题、内容、摘要字段设置不同权重，优化搜索结果排序
2. **相关性调优**：结合用户行为数据优化搜索结果排序
3. **搜索建议**：实现搜索关键词自动补全和纠错功能
4. **过滤搜索**：增加按标签、作者、时间范围过滤功能
