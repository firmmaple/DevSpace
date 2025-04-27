// Profile Activity and Stats Module
document.addEventListener('DOMContentLoaded', function() {
    // Get current user ID from AuthUtils
    const currentUser = AuthUtils.getUserInfo();
    if (!currentUser) return;
    
    // Initialize activity history tab
    setupActivityTab();
    
    // Initialize article stats section
    setupArticleStatsSection();
});

// Initialize user activity history
function setupActivityTab() {
    // Add a new tab for activity history
    addActivityTab();
    
    // Load activity history when tab is clicked
    const activityLink = document.querySelector('a[href="#activity-history"]');
    if (activityLink) {
        activityLink.addEventListener('click', function() {
            loadUserActivities(1);
        });
    }
    
    // Check if the current hash is set to activity-history and load if needed
    if (window.location.hash === '#activity-history') {
        loadUserActivities(1);
    }
}

// Add the activity history tab to the UI
function addActivityTab() {
    // Check if tab already exists
    if (document.getElementById('activity-history')) return;
    
    // Get the tab list and tab content container
    const tabList = document.querySelector('.list-group');
    const tabContent = document.querySelector('.tab-content');
    
    if (!tabList || !tabContent) return;
    
    // Add the tab link
    const activityLink = document.createElement('a');
    activityLink.href = '#activity-history';
    activityLink.className = 'list-group-item list-group-item-action';
    activityLink.setAttribute('data-bs-toggle', 'list');
    activityLink.innerHTML = '<i class="fas fa-history me-2"></i> Activity History';
    tabList.insertBefore(activityLink, document.querySelector('a[href="#security"]'));
    
    // Add the tab content
    const activityPane = document.createElement('div');
    activityPane.className = 'tab-pane fade';
    activityPane.id = 'activity-history';
    
    activityPane.innerHTML = `
        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white">
                <div class="d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">Activity History</h5>
                    <div class="btn-group" role="group" aria-label="Activity type filter">
                        <select id="activity-type-filter" class="form-select form-select-sm">
                            <option value="">All Activities</option>
                            <option value="CREATE_ARTICLE">Create Article</option>
                            <option value="EDIT_ARTICLE">Edit Article</option>
                            <option value="VIEW_ARTICLE">View Article</option>
                            <option value="LIKE_ARTICLE">Like Article</option>
                            <option value="COLLECT_ARTICLE">Collect Article</option>
                            <option value="COMMENT">Comments</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="card-body p-4">
                <div id="activities-container">
                    <!-- Activities will be loaded here -->
                    <div class="text-center py-5" id="activities-loading">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                        <p class="mt-2">Loading activity history...</p>
                    </div>
                    
                    <div id="activities-list" class="d-none">
                        <!-- Activities will be dynamically inserted here -->
                    </div>
                    
                    <!-- No activities message -->
                    <div id="no-activities" class="alert alert-info text-center d-none">
                        <i class="fas fa-info-circle me-2"></i> No activities to display.
                    </div>
                </div>
                
                <!-- Pagination -->
                <nav aria-label="Page navigation" class="my-4">
                    <ul class="pagination justify-content-center" id="activities-pagination">
                        <!-- Pagination will be generated dynamically -->
                    </ul>
                </nav>
            </div>
        </div>
    `;
    
    tabContent.appendChild(activityPane);
    
    // Set up activity filter dropdown
    setupActivityFilterDropdown();
}

// Set up activity filter dropdown
function setupActivityFilterDropdown() {
    const filterDropdown = document.getElementById('activity-type-filter');
    
    if (filterDropdown) {
        filterDropdown.addEventListener('change', function() {
            // Load activities with filter
            const activityType = this.value;
            loadUserActivities(1, activityType);
        });
    }
}

// Load user activities from API
function loadUserActivities(pageNum, activityType) {
    const activitiesLoading = document.getElementById('activities-loading');
    const activitiesList = document.getElementById('activities-list');
    const noActivities = document.getElementById('no-activities');
    
    if (!activitiesLoading || !activitiesList || !noActivities) return;
    
    // Show loading indicator
    activitiesLoading.classList.remove('d-none');
    activitiesList.classList.add('d-none');
    noActivities.classList.add('d-none');
    
    // Get the current user ID
    const currentUser = AuthUtils.getUserInfo();
    if (!currentUser || !currentUser.id) {
        activitiesLoading.classList.add('d-none');
        noActivities.innerHTML = '<i class="fas fa-info-circle me-2"></i>Please log in to view your activity history.';
        noActivities.classList.remove('d-none');
        return;
    }
    
    // Build API URL - try the /my-activities endpoint first
    let url = `/api/activities/user/${currentUser.id}?pageNum=${pageNum}&pageSize=10`;
    if (activityType) {
        url = `/api/activities/user/${currentUser.id}/type/${activityType}?pageNum=${pageNum}&pageSize=10`;
    }
    
    // Call API to get activities
    AuthUtils.authenticatedFetch(url)
        .then(data => {
            // Hide loading indicator
            activitiesLoading.classList.add('d-none');
            
            if (data.status.code === 0 && data.result) {
                const activities = data.result.records || [];
                
                if (activities.length > 0) {
                    // Clear activities list
                    activitiesList.innerHTML = '';
                    
                    // Add activities
                    activities.forEach(activity => {
                        const activityElement = createActivityElement(activity);
                        activitiesList.appendChild(activityElement);
                    });
                    
                    // Show activities list
                    activitiesList.classList.remove('d-none');
                    
                    // Update pagination
                    updateActivityPagination(data.result, activityType);
                } else {
                    // Show no activities message
                    noActivities.innerHTML = '<i class="fas fa-info-circle me-2"></i> No activities to display.';
                    noActivities.classList.remove('d-none');
                }
            } else {
                // API call failed
                console.error('Error loading activities:', data.status.msg);
                noActivities.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>Failed to load activities. Please try again later.';
                noActivities.classList.remove('d-none');
            }
        })
        .catch(error => {
            console.error('Error loading activities:', error);
            activitiesLoading.classList.add('d-none');
            noActivities.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>Failed to load activities. Please try again later.';
            noActivities.classList.remove('d-none');
        });
}

// Create an activity list item element
function createActivityElement(activity) {
    const element = document.createElement('div');
    element.className = 'activity-item card mb-3 border-0 shadow-sm';
    
    // Determine icon based on activity type
    let icon, actionText, targetUrl;
    
    switch (activity.activityType) {
        case 'CREATE_ARTICLE':
            icon = 'fas fa-plus-circle text-success';
            actionText = 'created an article';
            targetUrl = `/articles/${activity.targetId}`;
            break;
        case 'EDIT_ARTICLE':
            icon = 'fas fa-edit text-primary';
            actionText = 'updated an article';
            targetUrl = `/articles/${activity.targetId}`;
            break;
        case 'VIEW_ARTICLE':
            icon = 'fas fa-eye text-info';
            actionText = 'viewed an article';
            targetUrl = `/articles/${activity.targetId}`;
            break;
        case 'LIKE_ARTICLE':
            icon = 'fas fa-heart text-danger';
            actionText = 'liked an article';
            targetUrl = `/articles/${activity.targetId}`;
            break;
        case 'COLLECT_ARTICLE':
            icon = 'fas fa-bookmark text-warning';
            actionText = 'collected an article';
            targetUrl = `/articles/${activity.targetId}`;
            break;
        case 'COMMENT':
            icon = 'fas fa-comment text-secondary';
            actionText = 'commented on an article';
            targetUrl = `/articles/${activity.targetId}#comments`;
            break;
        default:
            icon = 'fas fa-circle text-secondary';
            actionText = 'performed an action';
            targetUrl = '#';
    }
    
    // For comment activities, add the comment content if available
    let commentContent = '';
    if (activity.activityType === 'COMMENT' && activity.content) {
        // Check if content is a JSON string and try to parse it
        let commentText = activity.content;
        try {
            // If content is a JSON string with a content field, extract it
            if (activity.content.startsWith('{') && activity.content.includes('content')) {
                const contentObj = JSON.parse(activity.content);
                if (contentObj.content) {
                    commentText = contentObj.content;
                }
            }
        } catch (e) {
            // If parsing fails, use the original content
            console.log('Failed to parse comment content:', e);
        }
        
        commentContent = `<div class="mt-1 p-2 bg-light rounded">${commentText}</div>`;
    }
    
    element.innerHTML = `
        <div class="card-body">
            <div class="d-flex">
                <div class="me-3">
                    <i class="${icon} fa-lg"></i>
                </div>
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-start">
                        <h6 class="mb-1">You ${actionText}</h6>
                        <small class="text-muted">${activity.timeAgo}</small>
                    </div>
                    <div class="mb-1">
                        <a href="${targetUrl}" class="text-decoration-none fw-bold">
                            ${activity.targetTitle || 'Unknown item'}
                        </a>
                    </div>
                    ${commentContent}
                </div>
            </div>
        </div>
    `;
    
    return element;
}

// Update activity pagination
function updateActivityPagination(pageData, activityType) {
    const paginationElement = document.getElementById('activities-pagination');
    if (!paginationElement) return;
    
    paginationElement.innerHTML = '';
    
    const total = pageData.total;
    const pages = pageData.pages;
    const current = pageData.current;
    
    if (pages <= 1) return;  // Only show pagination if more than one page
    
    // Previous page
    const prevLi = document.createElement('li');
    prevLi.className = `page-item${current === 1 ? ' disabled' : ''}`;
    
    const prevLink = document.createElement('a');
    prevLink.className = 'page-link';
    prevLink.href = '#';
    prevLink.setAttribute('aria-label', 'Previous');
    prevLink.innerHTML = '<span aria-hidden="true">&laquo;</span>';
    
    if (current > 1) {
        prevLink.addEventListener('click', function(e) {
            e.preventDefault();
            loadUserActivities(current - 1, activityType);
        });
    }
    
    prevLi.appendChild(prevLink);
    paginationElement.appendChild(prevLi);
    
    // Page numbers
    const startPage = Math.max(1, current - 2);
    const endPage = Math.min(pages, startPage + 4);
    
    for (let i = startPage; i <= endPage; i++) {
        const pageLi = document.createElement('li');
        pageLi.className = `page-item${i === current ? ' active' : ''}`;
        
        const pageLink = document.createElement('a');
        pageLink.className = 'page-link';
        pageLink.href = '#';
        pageLink.textContent = i;
        
        if (i !== current) {
            pageLink.addEventListener('click', function(e) {
                e.preventDefault();
                loadUserActivities(i, activityType);
            });
        }
        
        pageLi.appendChild(pageLink);
        paginationElement.appendChild(pageLi);
    }
    
    // Next page
    const nextLi = document.createElement('li');
    nextLi.className = `page-item${current === pages ? ' disabled' : ''}`;
    
    const nextLink = document.createElement('a');
    nextLink.className = 'page-link';
    nextLink.href = '#';
    nextLink.setAttribute('aria-label', 'Next');
    nextLink.innerHTML = '<span aria-hidden="true">&raquo;</span>';
    
    if (current < pages) {
        nextLink.addEventListener('click', function(e) {
            e.preventDefault();
            loadUserActivities(current + 1, activityType);
        });
    }
    
    nextLi.appendChild(nextLink);
    paginationElement.appendChild(nextLi);
}

// Set up the article stats section
function setupArticleStatsSection() {
    // Replace the empty activity card with stats
    const activityCard = document.querySelector('#profile .card:nth-of-type(2)');
    if (!activityCard) return;
    
    // Replace the card content with article stats
    activityCard.innerHTML = `
        <div class="card-header bg-white">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Article Statistics</h5>
                <div id="article-select-container" class="d-none">
                    <select id="article-select" class="form-select form-select-sm">
                        <option value="">Select an article...</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="card-body p-4">
            <div id="article-stats-loading" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading stats...</span>
                </div>
                <p class="mt-2">Loading statistics...</p>
            </div>
            
            <div id="article-stats-content" class="d-none">
                <!-- Article title -->
                <h6 id="stats-article-title" class="text-center mb-3 text-primary">Article Title</h6>
                
                <!-- Summary statistics -->
                <div class="row text-center mb-3">
                    <div class="col-4">
                        <div class="h4 mb-0" id="article-stats-views">0</div>
                        <div class="small text-muted">Total Views</div>
                    </div>
                    <div class="col-4">
                        <div class="h4 mb-0" id="article-stats-likes">0</div>
                        <div class="small text-muted">Total Likes</div>
                    </div>
                    <div class="col-4">
                        <div class="h4 mb-0" id="article-stats-collects">0</div>
                        <div class="small text-muted">Total Collects</div>
                    </div>
                </div>
                
                <!-- Stats period selector -->
                <div class="d-flex justify-content-end mb-3">
                    <div class="btn-group btn-group-sm" role="group">
                        <button type="button" class="btn btn-outline-secondary active" data-period="7">Week</button>
                        <button type="button" class="btn btn-outline-secondary" data-period="30">Month</button>
                        <button type="button" class="btn btn-outline-secondary" data-period="90">90 Days</button>
                    </div>
                </div>
                
                <!-- Stats charts -->
                <div>
                    <canvas id="article-stats-chart" height="200"></canvas>
                </div>
                
                <!-- No stats message -->
                <div id="no-stats" class="alert alert-info text-center mt-3 d-none">
                    <i class="fas fa-info-circle me-2"></i>
                    <span>You have no published articles yet. Publish an article to see statistics.</span>
                </div>
            </div>
        </div>
    `;
    
    // Load user's articles for the dropdown
    loadUserArticles();
    
    // Set up period selector buttons
    setupPeriodButtons();
}

// Load user's articles for the dropdown
function loadUserArticles() {
    const currentUser = AuthUtils.getUserInfo();
    if (!currentUser || !currentUser.id) return;
    
    // Get articles from API
    AuthUtils.authenticatedFetch(`/api/articles?pageNum=1&pageSize=20&authorId=${currentUser.id}&status=1`)
        .then(data => {
            if (data.status.code === 0 && data.result && data.result.records && data.result.records.length > 0) {
                const articles = data.result.records;
                
                // Get select element and container
                const articleSelect = document.getElementById('article-select');
                const articleSelectContainer = document.getElementById('article-select-container');
                
                if (!articleSelect || !articleSelectContainer) return;
                
                // Clear existing options and add "All Articles" option
                articleSelect.innerHTML = '<option value="all">All Articles</option>';
                
                // Add options for each article
                articles.forEach(article => {
                    const option = document.createElement('option');
                    option.value = article.id;
                    option.textContent = article.title;
                    articleSelect.appendChild(option);
                });
                
                // Show select container
                articleSelectContainer.classList.remove('d-none');
                
                // Add change event listener
                articleSelect.addEventListener('change', function() {
                    const articleId = this.value;
                    
                    // Get current period
                    const activePeriodBtn = document.querySelector('[data-period].active');
                    const period = activePeriodBtn ? parseInt(activePeriodBtn.dataset.period) : 30;
                    
                    if (articleId === 'all') {
                        // Load aggregate stats for all articles
                        loadAllArticlesStats(period);
                    } else if (articleId) {
                        // Load stats for selected article
                        loadArticleStats(period, articleId);
                    }
                });
                
                // Load aggregate stats for all articles by default
                loadAllArticlesStats(7);
            } else {
                // No articles found, show stats with empty state
                loadArticleStats(7);
            }
        })
        .catch(error => {
            console.error('Error loading user articles:', error);
            // Load stats with empty state
            loadArticleStats(7);
        });
}

// Load aggregate stats for all user articles
function loadAllArticlesStats(days = 30) {
    const statsLoading = document.getElementById('article-stats-loading');
    const statsContent = document.getElementById('article-stats-content');
    const noStats = document.getElementById('no-stats');
    const statsArticleTitle = document.getElementById('stats-article-title');
    
    if (!statsLoading || !statsContent) return;
    
    // Show loading indicator
    statsLoading.classList.remove('d-none');
    statsContent.classList.add('d-none');
    if (noStats) noStats.classList.add('d-none');
    
    // Get current user
    const currentUser = AuthUtils.getUserInfo();
    if (!currentUser || !currentUser.id) {
        statsLoading.classList.add('d-none');
        if (noStats) {
            noStats.innerHTML = '<i class="fas fa-info-circle me-2"></i>Please log in to view your article statistics.</span>';
            noStats.classList.remove('d-none');
            statsContent.classList.remove('d-none');
        }
        return;
    }

    // Set article select to "all" if it exists
    const articleSelect = document.getElementById('article-select');
    if (articleSelect) {
        articleSelect.value = 'all';
    }
    
    // Get user's articles first
    AuthUtils.authenticatedFetch(`/api/articles?pageNum=1&pageSize=10&authorId=${currentUser.id}&status=1`)
        .then(data => {
            if (data.status.code === 0 && data.result && data.result.records && data.result.records.length > 0) {
                const articles = data.result.records;
                
                // Aggregated stats values
                let totalViews = 0;
                let totalLikes = 0;
                let totalCollects = 0;
                
                // For chart data
                let aggregatedStats = {
                    dates: [],
                    dailyStats: {
                        views: [],
                        likes: [],
                        collects: []
                    },
                    title: 'All Your Articles'
                };
                
                // Track completion of all requests
                let completedRequests = 0;
                const totalRequests = articles.length;
                
                // Process each article
                articles.forEach(article => {
                    // Get stats for this article
                    AuthUtils.authenticatedFetch(`/api/stats/article/${article.id}?days=${days}`)
                        .then(statsData => {
                            completedRequests++;
                            
                            if (statsData.status.code === 0 && statsData.result) {
                                const articleStats = statsData.result;
                                
                                // Add to totals
                                totalViews += articleStats.totalViewCount || 0;
                                totalLikes += articleStats.totalLikeCount || 0;
                                totalCollects += articleStats.totalCollectCount || 0;
                                
                                // If this is the first article, use its dates for the chart
                                if (aggregatedStats.dates.length === 0 && articleStats.dates) {
                                    aggregatedStats.dates = articleStats.dates;
                                    
                                    // Initialize arrays with zeros
                                    aggregatedStats.dailyStats.views = new Array(articleStats.dates.length).fill(0);
                                    aggregatedStats.dailyStats.likes = new Array(articleStats.dates.length).fill(0);
                                    aggregatedStats.dailyStats.collects = new Array(articleStats.dates.length).fill(0);
                                }
                                
                                // Add this article's daily stats to the aggregated stats
                                if (articleStats.dailyStats) {
                                    for (let i = 0; i < aggregatedStats.dates.length; i++) {
                                        if (articleStats.dailyStats.views && articleStats.dailyStats.views[i]) {
                                            aggregatedStats.dailyStats.views[i] += articleStats.dailyStats.views[i];
                                        }
                                        
                                        if (articleStats.dailyStats.likes && articleStats.dailyStats.likes[i]) {
                                            aggregatedStats.dailyStats.likes[i] += articleStats.dailyStats.likes[i];
                                        }
                                        
                                        if (articleStats.dailyStats.collects && articleStats.dailyStats.collects[i]) {
                                            aggregatedStats.dailyStats.collects[i] += articleStats.dailyStats.collects[i];
                                        }
                                    }
                                }
                            }
                            
                            // Check if all requests are completed
                            if (completedRequests === totalRequests) {
                                // Update UI with aggregated stats
                                updateStatsUI(totalViews, totalLikes, totalCollects, aggregatedStats);
                            }
                        })
                        .catch(error => {
                            completedRequests++;
                            console.error(`Error loading stats for article ${article.id}:`, error);
                            
                            // Check if all requests are completed
                            if (completedRequests === totalRequests) {
                                // Update UI with aggregated stats (even with partial data)
                                updateStatsUI(totalViews, totalLikes, totalCollects, aggregatedStats);
                            }
                        });
                });
            } else {
                // No articles found
                statsLoading.classList.add('d-none');
                
                // Show no stats message
                if (noStats) {
                    noStats.innerHTML = '<i class="fas fa-info-circle me-2"></i><span>You have no published articles yet. Publish an article to see statistics.</span>';
                    noStats.classList.remove('d-none');
                    statsContent.classList.remove('d-none');
                }
            }
        })
        .catch(error => {
            console.error('Error loading user articles:', error);
            statsLoading.classList.add('d-none');
            
            // Show error message
            if (noStats) {
                noStats.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i><span>Failed to load article statistics.</span>';
                noStats.classList.remove('d-none');
                statsContent.classList.remove('d-none');
            }
        });
        
    // Helper function to update the UI with aggregated stats
    function updateStatsUI(totalViews, totalLikes, totalCollects, aggregatedStats) {
        // Hide loading indicator
        statsLoading.classList.add('d-none');
        
        // Set title for all articles
        if (statsArticleTitle) {
            statsArticleTitle.textContent = 'All Your Articles';
        }
        
        // Update summary numbers
        document.getElementById('article-stats-views').textContent = totalViews;
        document.getElementById('article-stats-likes').textContent = totalLikes;
        document.getElementById('article-stats-collects').textContent = totalCollects;
        
        // Create the chart
        createArticleStatsChart(aggregatedStats);
        
        // Show stats content
        statsContent.classList.remove('d-none');
    }
}

// Load article stats from API
function loadArticleStats(days = 30, specificArticleId = null) {
    const statsLoading = document.getElementById('article-stats-loading');
    const statsContent = document.getElementById('article-stats-content');
    const noStats = document.getElementById('no-stats');
    const statsArticleTitle = document.getElementById('stats-article-title');
    
    if (!statsLoading || !statsContent) return;
    
    // Show loading indicator
    statsLoading.classList.remove('d-none');
    statsContent.classList.add('d-none');
    if (noStats) noStats.classList.add('d-none');
    
    // Get current user
    const currentUser = AuthUtils.getUserInfo();
    if (!currentUser || !currentUser.id) {
        statsLoading.classList.add('d-none');
        if (noStats) {
            noStats.innerHTML = '<i class="fas fa-info-circle me-2"></i><span>Please log in to view your article statistics.</span>';
            noStats.classList.remove('d-none');
            statsContent.classList.remove('d-none');
        }
        return;
    }

    // If specific article ID is provided, use it directly
    if (specificArticleId) {
        // Update article select if it exists
        const articleSelect = document.getElementById('article-select');
        if (articleSelect) {
            articleSelect.value = specificArticleId;
        }
        
        // Load stats for the specific article
        fetchArticleStats(specificArticleId, days);
    } else {
        // First, get user's articles
        AuthUtils.authenticatedFetch(`/api/articles?pageNum=1&pageSize=1&authorId=${currentUser.id}&status=1`)
            .then(data => {
                if (data.status.code === 0 && data.result && data.result.records && data.result.records.length > 0) {
                    const article = data.result.records[0];
                    console.log("Found article for stats:", article.id);
                    
                    // Fetch stats for this article
                    fetchArticleStats(article.id, days);
                } else {
                    // No articles found
                    throw new Error('No published articles found');
                }
            })
            .catch(error => {
                console.error('Error loading article stats:', error);
                statsLoading.classList.add('d-none');
                
                // Show no stats message
                if (noStats) {
                    noStats.innerHTML = '<i class="fas fa-info-circle me-2"></i><span>You have no published articles yet. Publish an article to see statistics.</span>';
                    noStats.classList.remove('d-none');
                    statsContent.classList.remove('d-none');
                }
            });
    }
    
    // Helper function to fetch article stats
    function fetchArticleStats(articleId, days) {
        AuthUtils.authenticatedFetch(`/api/stats/article/${articleId}?days=${days}`)
            .then(data => {
                // Hide loading indicator
                statsLoading.classList.add('d-none');
                
                if (data.status.code === 0 && data.result) {
                    const statsData = data.result;
                    
                    // Update article title if exists
                    if (statsArticleTitle && statsData.title) {
                        statsArticleTitle.textContent = statsData.title;
                    }
                    
                    // Update summary numbers
                    document.getElementById('article-stats-views').textContent = statsData.totalViewCount || 0;
                    document.getElementById('article-stats-likes').textContent = statsData.totalLikeCount || 0;
                    document.getElementById('article-stats-collects').textContent = statsData.totalCollectCount || 0;
                    
                    // Create the chart
                    createArticleStatsChart(statsData);
                    
                    // Show stats content
                    statsContent.classList.remove('d-none');
                } else {
                    throw new Error('Failed to load statistics');
                }
            })
            .catch(error => {
                console.error('Error loading article stats:', error);
                statsLoading.classList.add('d-none');
                
                // Show no stats message
                if (noStats) {
                    noStats.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i><span>Failed to load statistics for this article.</span>';
                    noStats.classList.remove('d-none');
                    statsContent.classList.remove('d-none');
                }
            });
    }
}

// Create the article stats chart with Chart.js
function createArticleStatsChart(statsData) {
    const chartCanvas = document.getElementById('article-stats-chart');
    if (!chartCanvas) return;
    
    // Make sure Chart.js is loaded
    if (typeof Chart === 'undefined') {
        console.error('Chart.js is not loaded');
        const errorMsg = document.createElement('div');
        errorMsg.className = 'alert alert-warning';
        errorMsg.innerHTML = '<i class="fas fa-exclamation-triangle me-2"></i>Chart library could not be loaded.';
        chartCanvas.parentNode.replaceChild(errorMsg, chartCanvas);
        return;
    }
    
    // Clear any existing chart
    if (window.articleStatsChart) {
        window.articleStatsChart.destroy();
    }
    
    // Prepare chart data
    const dates = statsData.dates || [];
    const views = statsData.dailyStats?.views || [];
    const likes = statsData.dailyStats?.likes || [];
    const collects = statsData.dailyStats?.collects || [];
    
    // Format dates for display
    const formattedDates = dates.map(date => {
        const dateObj = new Date(date);
        return dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    });
    
    try {
        // Create chart
        window.articleStatsChart = new Chart(chartCanvas, {
            type: 'line',
            data: {
                labels: formattedDates,
                datasets: [
                    {
                        label: 'Views',
                        data: views,
                        borderColor: 'rgba(13, 110, 253, 1)',
                        backgroundColor: 'rgba(13, 110, 253, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Likes',
                        data: likes,
                        borderColor: 'rgba(220, 53, 69, 1)',
                        backgroundColor: 'rgba(220, 53, 69, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Collects',
                        data: collects,
                        borderColor: 'rgba(255, 193, 7, 1)',
                        backgroundColor: 'rgba(255, 193, 7, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0 // Only show integers
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error creating chart:', error);
        const errorMsg = document.createElement('div');
        errorMsg.className = 'alert alert-danger';
        errorMsg.innerHTML = '<i class="fas fa-exclamation-circle me-2"></i>Error creating chart. Please try again later.';
        chartCanvas.parentNode.replaceChild(errorMsg, chartCanvas);
    }
}

// Set up period selector buttons
function setupPeriodButtons() {
    const periodButtons = document.querySelectorAll('[data-period]');
    
    periodButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Update active state
            periodButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            // Get selected article ID
            const articleSelect = document.getElementById('article-select');
            const articleId = articleSelect ? articleSelect.value : 'all';
            
            // Load stats for selected period and article
            const period = parseInt(this.dataset.period);
            
            if (articleId === 'all') {
                loadAllArticlesStats(period);
            } else {
                loadArticleStats(period, articleId);
            }
        });
    });
} 