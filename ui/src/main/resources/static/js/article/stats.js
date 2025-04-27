// Article Stats Visualization Module
document.addEventListener('DOMContentLoaded', function() {
    // Get article ID from URL
    const urlParts = window.location.pathname.split('/');
    const articleId = urlParts[urlParts.length - 1];
    
    // Initialize stats with default period
    initStats();
    
    // Set up period button handlers
    setupPeriodButtons();
});

// Initialize the stats section
function initStats() {
    const statsCard = document.getElementById('article-stats-card');
    const statsLoading = document.getElementById('stats-loading');
    const statsContent = document.getElementById('stats-content');
    
    // Check if we're on a page with stats elements
    if (!statsCard || !statsLoading || !statsContent) return;
    
    // Get article ID from URL
    const urlParts = window.location.pathname.split('/');
    const articleId = urlParts[urlParts.length - 1];
    
    // Check if the current user is author or admin before showing stats card
    const currentUser = AuthUtils.getUserInfo();
    if (!currentUser) return;
    
    // Get article details to check if user is author or admin
    AuthUtils.authenticatedFetch(`/api/articles/${articleId}`)
        .then(data => {
            if (data.status.code === 0 && data.result) {
                const article = data.result;
                
                // Check if current user is the author or an admin
                const isAuthor = currentUser.id == article.authorId;
                const isAdmin = currentUser.roles && 
                              Array.isArray(currentUser.roles) && 
                              currentUser.roles.includes('ROLE_ADMIN');
                
                // Only show stats for the author or admin
                if (isAuthor || isAdmin) {
                    statsCard.classList.remove('d-none');
                    loadArticleStats(articleId, 7); // Default to 7 days
                }
            }
        })
        .catch(error => {
            console.error('Error checking article author status:', error);
        });
}

// Load article statistics from API
function loadArticleStats(articleId, days) {
    if (!articleId) return;
    
    const statsLoading = document.getElementById('stats-loading');
    const statsContent = document.getElementById('stats-content');
    
    if (!statsLoading || !statsContent) return;
    
    // Show loading indicator
    statsLoading.classList.remove('d-none');
    statsContent.classList.add('d-none');
    
    // Call stats API
    AuthUtils.authenticatedFetch(`/api/stats/article/${articleId}?days=${days}`)
        .then(data => {
            // Hide loading indicator
            statsLoading.classList.add('d-none');
            
            if (data.status.code === 0 && data.result) {
                const statsData = data.result;
                
                // Update summary numbers
                document.getElementById('stats-views').textContent = statsData.totalViewCount || 0;
                document.getElementById('stats-likes').textContent = statsData.totalLikeCount || 0;
                document.getElementById('stats-collects').textContent = statsData.totalCollectCount || 0;
                
                // Update weekly change indicators
                updateChangeIndicator('views-change', statsData.viewCountWeekOverWeek);
                updateChangeIndicator('likes-change', statsData.likeCountWeekOverWeek);
                updateChangeIndicator('collects-change', statsData.collectCountWeekOverWeek);
                
                // Create the chart
                createStatsChart(statsData);
                
                // Show stats content
                statsContent.classList.remove('d-none');
            } else {
                // Show error
                showStatsError("Failed to load statistics");
            }
        })
        .catch(error => {
            console.error('Error loading article stats:', error);
            statsLoading.classList.add('d-none');
            showStatsError("Error loading statistics: " + error.message);
        });
}

// Create the stats chart
function createStatsChart(statsData) {
    const chartCanvas = document.getElementById('stats-chart');
    if (!chartCanvas) return;
    
    // Clear any existing chart
    if (window.statsChart) {
        window.statsChart.destroy();
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
    
    // Create chart
    window.statsChart = new Chart(chartCanvas, {
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
}

// Update change indicator with proper formatting
function updateChangeIndicator(elementId, changeValue) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    if (changeValue === null || changeValue === undefined) {
        element.innerHTML = '<i class="fas fa-minus text-secondary"></i> 0%';
        return;
    }
    
    const changePercent = Math.abs(Math.round(changeValue * 100));
    
    if (changeValue > 0) {
        element.innerHTML = `<i class="fas fa-arrow-up text-success"></i> ${changePercent}%`;
        element.classList.add('text-success');
    } else if (changeValue < 0) {
        element.innerHTML = `<i class="fas fa-arrow-down text-danger"></i> ${changePercent}%`;
        element.classList.add('text-danger');
    } else {
        element.innerHTML = '<i class="fas fa-minus text-secondary"></i> 0%';
        element.classList.add('text-secondary');
    }
}

// Set up period selector buttons
function setupPeriodButtons() {
    const periodButtons = document.querySelectorAll('[data-period]');
    
    periodButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Get article ID
            const urlParts = window.location.pathname.split('/');
            const articleId = urlParts[urlParts.length - 1];
            
            // Get period value
            const period = parseInt(this.dataset.period);
            
            // Update active state
            periodButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            // Load stats for selected period
            loadArticleStats(articleId, period);
        });
    });
}

// Show error message in stats area
function showStatsError(message) {
    const statsContent = document.getElementById('stats-content');
    if (!statsContent) return;
    
    statsContent.innerHTML = `
        <div class="alert alert-danger">
            <i class="fas fa-exclamation-circle me-2"></i> ${message}
        </div>
    `;
    
    statsContent.classList.remove('d-none');
} 