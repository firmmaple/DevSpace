 /**
 * 文章列表页面控制器
 * 负责文章加载、分页、过滤和搜索功能
 */
const ArticleListController = (function() {
    // 私有变量 - DOM元素引用
    let elements = {};
    
    // 私有变量 - 分页和过滤状态
    let state = {
        currentPage: 1,
        pageSize: 5,
        authorId: null,
        status: null,
        keyword: null
    };
    
    // 初始化DOM元素引用
    function initElements() {
        elements = {
            container: document.getElementById('articles-container'),
            list: document.getElementById('article-list'),
            template: document.querySelector('.article-item'),
            loading: document.getElementById('loading'),
            noArticles: document.getElementById('no-articles'),
            pagination: document.getElementById('pagination'),
            paginationInfo: document.getElementById('pagination-info'),
            popularArticles: document.getElementById('popular-articles'),
            filterForm: document.getElementById('filter-form'),
            searchInput: document.getElementById('searchQuery'),
            statusSelect: document.getElementById('filterStatus')
        };
    }
    
    // 从URL参数初始化状态
    function initFromUrl() {
        state.keyword = getUrlParam('keyword') || null;
        state.currentPage = parseInt(getUrlParam('page') || '1', 10);
        state.status = getUrlParam('status') || null;
        
        // 初始化表单值
        if (state.keyword && elements.searchInput) {
            elements.searchInput.value = state.keyword;
        }
        
        if (state.status && elements.statusSelect) {
            elements.statusSelect.value = state.status;
        }
    }
    
    // 获取URL参数
    function getUrlParam(param) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    }
    
    // 更新URL参数
    function updateUrlParams() {
        const url = new URL(window.location);
        
        // 更新或删除关键词参数
        if (state.keyword) {
            url.searchParams.set('keyword', state.keyword);
        } else {
            url.searchParams.delete('keyword');
        }
        
        // 更新或删除页码参数（第1页不显示page参数）
        if (state.currentPage > 1) {
            url.searchParams.set('page', state.currentPage);
        } else {
            url.searchParams.delete('page');
        }
        
        // 更新或删除状态参数
        if (state.status) {
            url.searchParams.set('status', state.status);
        } else {
            url.searchParams.delete('status');
        }
        
        // 更新浏览器历史记录，但不刷新页面
        window.history.pushState({}, '', url);
    }
    
    // 处理表单提交
    function handleFormSubmit(e) {
        e.preventDefault();
        
        // 获取新的过滤条件
        const newKeyword = elements.searchInput.value.trim() || null;
        const newStatus = elements.statusSelect.value || null;
        
        // 如果过滤条件变化，重置页码为1
        if (newKeyword !== state.keyword || newStatus !== state.status) {
            state.currentPage = 1;
        }
        
        // 更新状态
        state.keyword = newKeyword;
        state.status = newStatus;
        
        // 加载文章并更新URL
        updateUrlParams();
        loadArticles();
    }
    
    // 显示/隐藏加载状态
    function showLoading(show) {
        if (show) {
            elements.loading.classList.remove('d-none');
            if (elements.list) {
                elements.list.classList.add('d-none');
            }
        } else {
            elements.loading.classList.add('d-none');
        }
    }
    
    // 构建API URL
    function buildApiUrl() {
        if (state.keyword) {
            // 搜索API
            return `/api/article/search?keyword=${encodeURIComponent(state.keyword)}&page=${state.currentPage - 1}&size=${state.pageSize}`;
        } else {
            // 标准列表API
            let url = `/api/articles?pageNum=${state.currentPage}&pageSize=${state.pageSize}`;
            if (state.authorId) url += `&authorId=${state.authorId}`;
            if (state.status) url += `&status=${state.status}`;
            return url;
        }
    }
    
    // 加载文章
    function loadArticles() {
        showLoading(true);
        
        const url = buildApiUrl();
        console.log('加载文章，页码:', state.currentPage, '请求URL:', url);
        
        // 使用认证工具发送请求
        if (typeof AuthUtils === 'undefined') {
            console.error('AuthUtils未定义，使用标准fetch作为后备');
            fetch(url)
                .then(response => response.json())
                .then(data => processApiResponse(data))
                .catch(error => handleApiError(error));
        } else {
            AuthUtils.authenticatedFetch(url)
                .then(data => processApiResponse(data))
                .catch(error => handleApiError(error));
        }
    }
    
    // 处理API响应并更新URL
    function processApiResponse(data) {
        console.log('API响应数据:', data);
        handleArticlesResponse(data);
        updateUrlParams();
    }
    
    // 解析API响应数据
    function parseApiResponse(data) {
        let articles = [];
        let pageData = null;
        
        if (data.status.code === 0) {
            if (state.keyword && data.result.content) {
                // 搜索API格式 (Page对象)
                articles = data.result.content;
                pageData = {
                    total: data.result.totalElements,
                    pages: data.result.totalPages,
                    current: data.result.number + 1,
                    size: data.result.size,
                    records: data.result.content
                };
            } else if (data.result.records) {
                // 标准API响应格式 (IPage对象)
                articles = data.result.records;
                pageData = data.result;
            }
        }
        
        return { articles, pageData };
    }
    
    // 替换文章列表
    function replaceArticleList(newList) {
        if (elements.list && elements.list.parentNode) {
            elements.list.parentNode.replaceChild(newList, elements.list);
        } else {
            elements.container.querySelector('#article-list')?.remove();
            elements.container.insertBefore(newList, elements.pagination.parentNode);
        }
    }
    
    // 显示无文章信息
    function showNoArticlesMessage(container) {
        const noArticlesClone = elements.noArticles.cloneNode(true);
        noArticlesClone.classList.remove('d-none');
        
        if (state.keyword) {
            noArticlesClone.innerHTML = `<i class="fas fa-info-circle me-2"></i> 没有找到与 "<strong>${state.keyword}</strong>" 相关的文章。`;
        } else {
            noArticlesClone.innerHTML = '<i class="fas fa-info-circle me-2"></i> 暂无文章。';
        }
        
        container.appendChild(noArticlesClone);
        elements.pagination.innerHTML = '';
    }
    
    // 渲染文章列表
    function renderArticles(articles, container) {
        articles.forEach(article => {
            const articleElement = createArticleElement(article);
            container.appendChild(articleElement);
        });
        
        container.classList.remove('d-none');
        elements.noArticles.classList.add('d-none');
    }
    
    // 处理文章列表响应
    function handleArticlesResponse(data) {
        showLoading(false);
        
        // 解析API响应数据
        const { articles, pageData } = parseApiResponse(data);
        
        // 创建新的文章列表容器
        const articleListContainer = document.createElement('div');
        articleListContainer.id = 'article-list';
        
        if (articles && articles.length > 0) {
            console.log(`渲染${articles.length}篇文章，当前页码：${pageData.current}`);
            
            // 渲染文章列表
            renderArticles(articles, articleListContainer);
            
            // 更新分页
            if (pageData) {
                updatePagination(pageData);
            }
        } else {
            // 显示无文章信息
            showNoArticlesMessage(articleListContainer);
        }
        
        // 替换文章列表
        replaceArticleList(articleListContainer);
    }
    
    // 处理API错误
    function handleApiError(error) {
        console.error('加载文章失败:', error);
        showLoading(false);
        
        // 显示错误信息
        const newListContainer = document.createElement('div');
        newListContainer.id = 'article-list';
        
        const errorMessage = elements.noArticles.cloneNode(true);
        errorMessage.classList.remove('d-none');
        errorMessage.classList.add('alert-danger');
        errorMessage.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i> 加载文章失败，请稍后再试。';
        
        newListContainer.appendChild(errorMessage);
        replaceArticleList(newListContainer);
        
        // 清空分页
        elements.pagination.innerHTML = '';
    }
    
    // 创建文章元素
    function createArticleElement(article) {
        const articleClone = elements.template.cloneNode(true);
        
        // 设置文章标题
        const titleElement = articleClone.querySelector('.article-title');
        titleElement.textContent = article.title;
        
        // 高亮关键词
        highlightKeyword(titleElement, article.title, state.keyword);
        
        // 设置文章摘要
        const summaryElement = articleClone.querySelector('.article-summary');
        const summary = article.summary || (article.content ? article.content.substring(0, 150) + '...' : '无摘要');
        summaryElement.textContent = summary;
        
        // 高亮关键词
        highlightKeyword(summaryElement, summary, state.keyword);
        
        // 设置其他信息
        articleClone.querySelector('.article-author').textContent = article.authorUsername || 'Anonymous';
        articleClone.querySelector('.article-date').textContent = formatDate(article.createdAt);
        articleClone.querySelector('.article-views').textContent = article.viewCount || 0;
        articleClone.querySelector('.article-likes').textContent = article.likeCount || 0;
        articleClone.querySelector('.article-collects').textContent = article.collectCount || 0;
        
        // 设置文章链接
        const links = articleClone.querySelectorAll('.article-link');
        links.forEach(link => {
            link.href = `/articles/${article.id}`;
        });
        
        // 设置文章标签
        if (article.tags && article.tags.length > 0) {
            const tagsContainer = articleClone.querySelector('.article-tags');
            tagsContainer.innerHTML = '';
            article.tags.forEach(tag => {
                const tagElement = document.createElement('span');
                tagElement.classList.add('badge', 'bg-secondary', 'me-1');
                tagElement.textContent = tag;
                tagsContainer.appendChild(tagElement);
            });
        } else {
            articleClone.querySelector('.article-tags').classList.add('d-none');
        }
        
        return articleClone;
    }
    
    // 高亮关键词
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
    
    // 更新分页
    function updatePagination(pageData) {
        elements.pagination.innerHTML = '';
        
        // 如果只有一页，只显示总数
        if (pageData.pages <= 1) {
            if (elements.paginationInfo) {
                if (pageData.total > 0) {
                    elements.paginationInfo.textContent = `共 ${pageData.total} 篇文章`;
                } else {
                    elements.paginationInfo.textContent = '';
                }
            }
            return;
        }
        
        const pages = pageData.pages;
        
        // 同步当前页码
        if (pageData.current && pageData.current !== state.currentPage) {
            state.currentPage = pageData.current;
        }
        
        // 更新分页信息
        if (elements.paginationInfo) {
            const total = pageData.total || (pageData.records ? pageData.records.length : 0);
            elements.paginationInfo.textContent = `第 ${state.currentPage} 页，共 ${pages} 页，总计 ${total} 篇文章`;
        }
        
        // 创建分页项
        const paginationItems = createPaginationItems(pages, state.currentPage);
        
        // 将分页项添加到导航
        paginationItems.forEach(item => elements.pagination.appendChild(item));
    }
    
    // 创建分页项列表
    function createPaginationItems(totalPages, currentPage) {
        const items = [];
        
        // 首页按钮
        items.push(createPaginationItem('首页', 1, currentPage === 1));
        
        // 上一页按钮
        items.push(createPaginationItem('<span aria-hidden="true">&laquo;</span>', 
            currentPage - 1, currentPage === 1, 'Previous'));
        
        // 计算页码范围
        const {startPage, endPage} = calculatePageRange(currentPage, totalPages);
        
        // 添加第一页和省略号（如果需要）
        if (startPage > 1) {
            items.push(createPaginationItem('1', 1));
            if (startPage > 2) {
                items.push(createEllipsisItem());
            }
        }
        
        // 添加页码按钮
        for (let i = startPage; i <= endPage; i++) {
            items.push(createPaginationItem(i.toString(), i, false, null, i === currentPage));
        }
        
        // 添加最后一页和省略号（如果需要）
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                items.push(createEllipsisItem());
            }
            items.push(createPaginationItem(totalPages.toString(), totalPages));
        }
        
        // 下一页按钮
        items.push(createPaginationItem('<span aria-hidden="true">&raquo;</span>', 
            currentPage + 1, currentPage === totalPages, 'Next'));
        
        // 末页按钮
        items.push(createPaginationItem('末页', totalPages, currentPage === totalPages));
        
        return items;
    }
    
    // 计算页码范围
    function calculatePageRange(currentPage, totalPages) {
        let startPage, endPage;
        
        if (totalPages <= 5) {
            // 如果总页数小于等于5，显示所有页码
            startPage = 1;
            endPage = totalPages;
        } else {
            // 如果总页数大于5，使用滑动窗口
            if (currentPage <= 3) {
                startPage = 1;
                endPage = 5;
            } else if (currentPage + 2 >= totalPages) {
                startPage = totalPages - 4;
                endPage = totalPages;
            } else {
                startPage = currentPage - 2;
                endPage = currentPage + 2;
            }
        }
        
        return {startPage, endPage};
    }
    
    // 创建分页项
    function createPaginationItem(text, pageNum, disabled = false, ariaLabel = null, active = false) {
        const item = document.createElement('li');
        item.classList.add('page-item');
        if (disabled) item.classList.add('disabled');
        if (active) item.classList.add('active');
        
        const link = document.createElement('a');
        link.classList.add('page-link');
        link.href = '#';
        link.innerHTML = text;
        
        if (ariaLabel) {
            link.setAttribute('aria-label', ariaLabel);
        }
        
        link.addEventListener('click', function(e) {
            e.preventDefault();
            if (!disabled && !active) {
                state.currentPage = pageNum;
                loadArticles();
                window.scrollTo({top: 0, behavior: 'smooth'});
            }
        });
        
        item.appendChild(link);
        return item;
    }
    
    // 创建省略号项
    function createEllipsisItem() {
        const item = document.createElement('li');
        item.classList.add('page-item', 'disabled');
        
        const span = document.createElement('a');
        span.classList.add('page-link');
        span.textContent = '...';
        
        item.appendChild(span);
        return item;
    }
    
    // 加载热门文章
    function loadPopularArticles() {
        if (!elements.popularArticles) return;
        
        AuthUtils.authenticatedFetch(`/api/articles?pageNum=1&pageSize=5`)
            .then(data => {
                elements.popularArticles.innerHTML = '';
                
                if (data.status.code === 0 && data.result && data.result.records.length > 0) {
                    // 显示热门文章
                    data.result.records.forEach(article => {
                        const articleElement = createPopularArticleElement(article);
                        elements.popularArticles.appendChild(articleElement);
                    });
                } else {
                    // 无热门文章
                    elements.popularArticles.innerHTML = '<div class="list-group-item text-center text-muted py-3">暂无热门文章</div>';
                }
            })
            .catch(error => {
                console.error('加载热门文章失败:', error);
                elements.popularArticles.innerHTML = '<div class="list-group-item text-center text-danger py-3">加载失败，请稍后再试</div>';
            });
    }
    
    // 创建热门文章元素
    function createPopularArticleElement(article) {
        const articleElement = document.createElement('a');
        articleElement.classList.add('list-group-item', 'list-group-item-action');
        articleElement.href = `/articles/${article.id}`;
        
        const title = document.createElement('div');
        title.classList.add('fw-bold');
        title.textContent = article.title;
        
        const stats = document.createElement('div');
        stats.classList.add('d-flex', 'justify-content-between', 'small', 'text-muted', 'mt-1');
        stats.innerHTML = `
            <span><i class="fas fa-eye me-1"></i> ${article.viewCount || 0}</span>
            <span><i class="fas fa-heart me-1"></i> ${article.likeCount || 0}</span>
        `;
        
        articleElement.appendChild(title);
        articleElement.appendChild(stats);
        
        return articleElement;
    }
    
    // 格式化日期
    function formatDate(dateString) {
        if (!dateString) return 'Unknown';
        
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }
    
    // 检查认证状态
    function checkAuthStatus() {
        const userInfo = getCookie('user_info');
        const authRequired = document.querySelectorAll('.auth-required');
        
        authRequired.forEach(el => {
            if (userInfo) {
                el.classList.remove('force-hide');
                el.classList.add('force-show');
            } else {
                el.classList.remove('force-show');
                el.classList.add('force-hide');
            }
        });
    }
    
    // 获取Cookie
    function getCookie(name) {
        const cookies = document.cookie.split(';');
        for (let i = 0; i < cookies.length; i++) {
            const cookie = cookies[i].trim();
            if (cookie.startsWith(name + '=')) {
                return decodeURIComponent(cookie.substring(name.length + 1));
            }
        }
        return null;
    }
    
    // 注册事件处理器
    function registerEventHandlers() {
        if (elements.filterForm) {
            elements.filterForm.addEventListener('submit', handleFormSubmit);
        }
    }
    
    // 初始化
    function init() {
        initElements();
        initFromUrl();
        
        // 移除模板
        if (elements.template) {
            elements.template.remove();
        }
        
        registerEventHandlers();
        loadArticles();
        loadPopularArticles();
        checkAuthStatus();
    }
    
    // 返回公共API
    return {
        init: init
    };
})();

// 页面加载完成后初始化控制器
document.addEventListener('DOMContentLoaded', function() {
    ArticleListController.init();
});