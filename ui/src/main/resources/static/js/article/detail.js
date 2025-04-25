// Global variables
let currentUser = null;
let articleId = null;
let isAuthor = false;
let isLiked = false;
let isCollected = false;

document.addEventListener('DOMContentLoaded', function() {
    initPage();
});

// Initialize the page
function initPage() {
    // Get article ID from URL
    const urlParts = window.location.pathname.split('/');
    articleId = urlParts[urlParts.length - 1];
    
    // Set up event handlers with null checks
    const commentForm = document.getElementById('comment-form');
    if (commentForm) {
        commentForm.addEventListener('submit', handleCommentSubmit);
    }
    
    const loadRelatedBtn = document.getElementById('load-related-btn');
    if (loadRelatedBtn) {
        loadRelatedBtn.addEventListener('click', loadRelatedArticles);
    }
    
    // Check authentication status
    checkAuthStatus();
    
    // Load article details
    loadArticleDetails();
    
    // Load comments
    loadComments();
    
    // Set up buttons
    setupDeleteButton();
    setupInteractionButtons();
}

// Set up the delete button functionality
function setupDeleteButton() {
    const deleteButton = document.getElementById('delete-button');
    const confirmDeleteButton = document.getElementById('confirm-delete');
    
    if (deleteButton) {
        deleteButton.addEventListener('click', function() {
            const deleteModal = document.getElementById('deleteModal');
            if (deleteModal) {
                new bootstrap.Modal(deleteModal).show();
            }
        });
    }
    
    if (confirmDeleteButton) {
        confirmDeleteButton.addEventListener('click', function() {
            AuthUtils.authenticatedFetch(`/api/articles/${articleId}`, {
                method: 'DELETE'
            })
            .then(data => {
                if (data.status.code === 0) {
                    // Redirect to articles list
                    window.location.href = '/articles';
                }
            })
            .catch(error => console.error('Error deleting article:', error));
        });
    }
}

// Set up interaction buttons (like, collect)
function setupInteractionButtons() {
    const likeButton = document.getElementById('like-button');
    const collectButton = document.getElementById('collect-button');
    
    // Handle like button click
    if (likeButton) {
        likeButton.addEventListener('click', function() {
            if (!AuthUtils.isAuthenticated()) {
                window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
                return;
            }
            
            // Toggle like state optimistically
            isLiked = !isLiked;
            updateLikeUI();
            
            // Send request to server
            const method = isLiked ? 'POST' : 'DELETE';
            AuthUtils.authenticatedFetch(`/api/articles/${articleId}/like`, {
                method: method
            })
            .then(data => {
                if (data.status.code !== 0) {
                    // Revert if error
                    isLiked = !isLiked;
                    updateLikeUI();
                }
            })
            .catch(error => {
                console.error('Error toggling like:', error);
                // Revert on error
                isLiked = !isLiked;
                updateLikeUI();
            });
        });
    }
    
    // Handle collect button click
    if (collectButton) {
        collectButton.addEventListener('click', function() {
            if (!AuthUtils.isAuthenticated()) {
                window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
                return;
            }
            
            // Toggle collect state optimistically
            isCollected = !isCollected;
            updateCollectUI();
            
            // Send request to server
            const method = isCollected ? 'POST' : 'DELETE';
            AuthUtils.authenticatedFetch(`/api/articles/${articleId}/collect`, {
                method: method
            })
            .then(data => {
                if (data.status.code !== 0) {
                    // Revert if error
                    isCollected = !isCollected;
                    updateCollectUI();
                }
            })
            .catch(error => {
                console.error('Error toggling collect:', error);
                // Revert on error
                isCollected = !isCollected;
                updateCollectUI();
            });
        });
    }
}

// Update UI for like button
function updateLikeUI() {
    const likeButton = document.getElementById('like-button');
    if (!likeButton) return;
    
    const likeIcon = likeButton.querySelector('i');
    const likeText = likeButton.querySelector('span');
    const likeCount = document.getElementById('article-likes');
    
    if (isLiked) {
        likeButton.classList.add('btn-danger');
        likeButton.classList.remove('btn-outline-danger');
        likeIcon.className = 'fas fa-heart me-1';
        likeText.textContent = 'Liked';
    } else {
        likeButton.classList.remove('btn-danger');
        likeButton.classList.add('btn-outline-danger');
        likeIcon.className = 'far fa-heart me-1';
        likeText.textContent = 'Like';
    }
}

// Update UI for collect button
function updateCollectUI() {
    const collectButton = document.getElementById('collect-button');
    if (!collectButton) return;
    
    const collectIcon = collectButton.querySelector('i');
    const collectText = collectButton.querySelector('span');
    
    if (isCollected) {
        collectButton.classList.add('btn-warning');
        collectButton.classList.remove('btn-outline-warning');
        collectIcon.className = 'fas fa-bookmark me-1';
        collectText.textContent = 'Collected';
    } else {
        collectButton.classList.remove('btn-warning');
        collectButton.classList.add('btn-outline-warning');
        collectIcon.className = 'far fa-bookmark me-1';
        collectText.textContent = 'Collect';
    }
}

// Check if user is authenticated
function checkAuthStatus() {
    // Use AuthUtils to check authentication status
    const isAuthenticated = AuthUtils.isAuthenticated();
    const authRequired = document.querySelectorAll('.auth-required');
    const loginToComment = document.getElementById('login-to-comment');
    
    if (isAuthenticated) {
        // User is logged in
        authRequired.forEach(el => {
            el.classList.remove('force-hide');
            el.classList.add('force-show');
        });
        if (loginToComment) {
            loginToComment.classList.add('d-none');
        }
        
        // Get user info from AuthUtils
        currentUser = AuthUtils.getUserInfo();
    } else {
        // User is not logged in
        authRequired.forEach(el => {
            el.classList.remove('force-show');
            el.classList.add('force-hide');
        });
        if (loginToComment) {
            loginToComment.classList.remove('d-none');
        }
    }
}

// Load article details
function loadArticleDetails() {
    const articleContainer = document.getElementById('article-container');
    const articleLoading = document.getElementById('article-loading');
    const articleNotFound = document.getElementById('article-not-found');
    const authorActions = document.getElementById('author-actions');
    
    // Early return if essential elements don't exist
    if (!articleContainer || !articleLoading) return;
    
    // Show loading
    articleLoading.classList.remove('d-none');
    articleContainer.classList.add('d-none');
    if (articleNotFound) {
        articleNotFound.classList.add('d-none');
    }
    
    AuthUtils.authenticatedFetch(`/api/articles/${articleId}`)
        .then(data => {
            articleLoading.classList.add('d-none');
            
            if (data.status.code === 0 && data.result) {
                const article = data.result;
                
                // Debug output the complete article object
                console.log("Article data:", article);
                
                // Populate article details with null checks
                const titleElement = document.getElementById('article-title');
                const contentElement = document.getElementById('article-content');
                const authorNameElement = document.getElementById('author-name');
                const authorAvatarElement = document.getElementById('author-avatar');
                const dateElement = document.getElementById('article-date');
                const viewsElement = document.getElementById('article-views');
                const likesElement = document.getElementById('article-likes');
                const collectsElement = document.getElementById('article-collects');
                
                if (titleElement) titleElement.textContent = article.title;
                if (contentElement) contentElement.innerHTML = article.content;
                if (authorNameElement) authorNameElement.textContent = article.authorUsername || 'Unknown';
                if (authorAvatarElement) {
                    authorAvatarElement.src = article.authorAvatarUrl || 'https://via.placeholder.com/40';
                    authorAvatarElement.alt = article.authorUsername || 'Author';
                }
                if (dateElement) dateElement.textContent = formatDate(article.createdAt);
                if (viewsElement) viewsElement.textContent = article.viewCount || 0;
                if (likesElement) likesElement.textContent = article.likeCount || 0;
                if (collectsElement) collectsElement.textContent = article.collectCount || 0;
                
                // Populate tags
                const tagsContainer = document.getElementById('article-tags');
                if (tagsContainer && article.tags && article.tags.length > 0) {
                    tagsContainer.innerHTML = '';
                    article.tags.forEach(tag => {
                        const tagElement = document.createElement('a');
                        tagElement.classList.add('badge', 'bg-secondary', 'me-1', 'text-decoration-none');
                        tagElement.href = `/articles?tag=${tag}`;
                        tagElement.textContent = tag;
                        tagsContainer.appendChild(tagElement);
                    });
                }
                
                // Update Meta tags
                document.title = article.title + ' - DevSpace';
                const metaDescription = document.querySelector('meta[name="description"]');
                if (metaDescription) {
                    metaDescription.setAttribute('content', article.summary || article.title);
                }
                
                // Populate author card
                const authorCard = document.getElementById('author-card');
                const authorCardAvatar = document.getElementById('author-card-avatar');
                const authorCardName = document.getElementById('author-card-name');
                const authorCardBio = document.getElementById('author-card-bio');
                const authorCardProfile = document.getElementById('author-card-profile');
                
                if (authorCard) {
                    // Show author card
                    authorCard.classList.remove('d-none');
                    
                    // Populate author card details with data from API
                    if (authorCardName) {
                        authorCardName.textContent = article.authorUsername || 'Unknown';
                    }
                    
                    if (authorCardAvatar) {
                        authorCardAvatar.src = article.authorAvatarUrl || 'https://via.placeholder.com/100';
                        authorCardAvatar.alt = article.authorUsername || 'Author';
                    }
                    
                    if (authorCardBio) {
                        authorCardBio.textContent = article.authorBio || 'No bio available';
                    }
                    
                    if (authorCardProfile && article.authorId) {
                        authorCardProfile.href = `/users/${article.authorId}`;
                    }
                }
                
                // Show article
                articleContainer.classList.remove('d-none');
                
                // Set author actions
                if (currentUser && currentUser.sub == article.authorId) {
                    isAuthor = true;
                    if (authorActions) authorActions.classList.remove('d-none');
                    
                    // Update edit button href
                    const editButton = document.getElementById('edit-button');
                    if (editButton) {
                        editButton.href = `/articles/edit/${articleId}`;
                    }
                }
                
                // Set interaction states
                isLiked = article.likedByCurrentUser || false;
                isCollected = article.collectedByCurrentUser || false;
                
                // Update UI states
                updateLikeUI();
                updateCollectUI();
                
                // Load related articles based on tags
                if (article.tags && article.tags.length > 0) {
                    loadRelatedArticles(article.tags);
                } else {
                    loadRelatedArticles(); // Load default related articles
                }
            } else {
                // Article not found
                if (articleNotFound) {
                    articleNotFound.classList.remove('d-none');
                }
            }
        })
        .catch(error => {
            console.error('Error loading article:', error);
            articleLoading.classList.add('d-none');
            if (articleNotFound) {
                articleNotFound.classList.remove('d-none');
            }
        });
}

// Load comments
function loadComments() {
    const commentsList = document.getElementById('comments-list');
    const loadingIndicator = document.getElementById('comments-loading');
    const noComments = document.getElementById('no-comments');
    
    if (!commentsList) return;
    
    // Show loading indicator
    if (loadingIndicator) {
        loadingIndicator.classList.remove('d-none');
    }
    
    // Hide no comments message
    if (noComments) {
        noComments.classList.add('d-none');
    }
    
    // Clear comments list but preserve the loading indicator
    if (loadingIndicator && commentsList.contains(loadingIndicator)) {
        // If loading indicator is in the comments list, remove all other children
        const children = Array.from(commentsList.children);
        children.forEach(child => {
            if (child !== loadingIndicator) {
                child.remove();
            }
        });
    } else {
        // Otherwise, just clear everything
        commentsList.innerHTML = '';
    }
    
    // Load comments from API - using new nested structure
    AuthUtils.authenticatedFetch(`/api/comments/article/${articleId}`)
        .then(data => {
            // Hide loading indicator
            if (loadingIndicator) {
                loadingIndicator.classList.add('d-none');
            }
            
            if (data.status.code === 0) {
                const commentsData = data.result;
                const commentCount = document.getElementById('comment-count');
                
                // Count total comments including all nested replies recursively
                let totalComments = 0;
                if (commentsData && commentsData.length > 0) {
                    totalComments = commentsData.length;
                    
                    // Count all nested replies recursively
                    commentsData.forEach(comment => {
                        totalComments += countNestedReplies(comment.replies || []);
                    });
                }
                
                // Update comment count
                if (commentCount) {
                    commentCount.textContent = totalComments;
                }
                
                // Render comments
                if (commentsData && commentsData.length > 0) {
                    commentsData.forEach(comment => {
                        renderComment(comment, commentsList);
                    });
                } else {
                    // Show no comments message
                    if (noComments) {
                        noComments.classList.remove('d-none');
                    }
                }
            }
        })
        .catch(error => {
            console.error('Error loading comments:', error);
            
            // Hide loading indicator
            if (loadingIndicator) {
                loadingIndicator.classList.add('d-none');
            }
            
            // Show error message
            const errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-danger';
            errorDiv.textContent = 'Failed to load comments: ' + error.message;
            commentsList.appendChild(errorDiv);
        });
}

// Count all nested replies recursively
function countNestedReplies(replies) {
    if (!replies || replies.length === 0) return 0;
    
    let count = replies.length;
    
    // Count nested replies recursively
    replies.forEach(reply => {
        if (reply.replies && reply.replies.length > 0) {
            count += countNestedReplies(reply.replies);
        }
    });
    
    return count;
}

// Render a single comment
function renderComment(comment, container, isPending = false) {
    const commentElement = document.createElement('div');
    commentElement.classList.add('comment-item', 'mb-3');
    commentElement.dataset.id = comment.id;
    
    if (isPending) {
        commentElement.classList.add('comment-pending');
    }
    
    const commentDate = formatDate(comment.createdAt);
    const isCommentOwner = currentUser && currentUser.sub == comment.userId;
    
    commentElement.innerHTML = `
        <div class="card ${isPending ? 'border-primary' : ''}">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <div class="d-flex align-items-center">
                        <img src="${comment.avatarUrl || 'https://via.placeholder.com/32'}" class="rounded-circle me-2" width="32" height="32" style="object-fit: cover;" alt="${comment.username || 'User'}">
                        <div>
                            <div class="fw-bold">${comment.username || 'Anonymous'}</div>
                            <div class="text-muted small">
                                ${commentDate}
                                ${isPending ? '<span class="ms-2 badge bg-primary">Posting...</span>' : ''}
                            </div>
                        </div>
                    </div>
                    ${!isPending && isCommentOwner ? `<button class="btn btn-sm btn-outline-danger delete-comment-btn" data-id="${comment.id}">
                        <i class="fas fa-trash-alt"></i>
                    </button>` : ''}
                </div>
                <p class="mb-2">${comment.content}</p>
                <div class="mt-2">
                    ${!isPending ? `<button class="btn btn-sm btn-outline-secondary reply-btn" data-id="${comment.id}" data-username="${comment.username || 'Anonymous'}">
                        <i class="fas fa-reply me-1"></i> Reply
                    </button>` : ''}
                </div>
            </div>
        </div>
    `;
    
    // Add event listeners only if not a pending comment
    if (!isPending) {
        // Add event listener for delete button
        const deleteBtn = commentElement.querySelector('.delete-comment-btn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', function() {
                const commentId = this.dataset.id;
                if (confirm('Are you sure you want to delete this comment?')) {
                    deleteComment(commentId);
                }
            });
        }
        
        // Add event listener for reply button
        const replyBtn = commentElement.querySelector('.reply-btn');
        if (replyBtn) {
            replyBtn.addEventListener('click', function() {
                const commentId = this.dataset.id;
                const username = this.dataset.username;
                showReplyForm(commentId, username, commentElement);
            });
        }
        
        // Render replies if any
        if (comment.replies && comment.replies.length > 0) {
            const repliesContainer = document.createElement('div');
            repliesContainer.classList.add('replies-container', 'ms-4', 'mt-2');
            
            comment.replies.forEach(reply => {
                renderReply(reply, repliesContainer);
            });
            
            commentElement.appendChild(repliesContainer);
        }
    }
    
    // Insert at the beginning of the comments list if pending, otherwise append
    if (isPending) {
        container.insertBefore(commentElement, container.firstChild);
    } else {
        container.appendChild(commentElement);
    }
    
    return commentElement;
}

// Render a reply (supports nested replies)
function renderReply(reply, container) {
    const replyElement = document.createElement('div');
    replyElement.classList.add('reply-item', 'mb-2');
    replyElement.dataset.id = reply.id;
    
    const replyDate = formatDate(reply.createdAt);
    const isReplyOwner = currentUser && currentUser.sub == reply.userId;
    
    replyElement.innerHTML = `
        <div class="card">
            <div class="card-body py-2 px-3">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <div class="d-flex align-items-center">
                        <img src="${reply.avatarUrl || 'https://via.placeholder.com/24'}" class="rounded-circle me-2" width="24" height="24" style="object-fit: cover;" alt="${reply.username || 'User'}">
                        <div>
                            <div class="fw-bold">${reply.username || 'Anonymous'}</div>
                            <div class="text-muted small">${replyDate}</div>
                        </div>
                    </div>
                    <div>
                        ${isReplyOwner ? `<button class="btn btn-sm btn-link text-danger delete-reply-btn p-0 me-2" data-id="${reply.id}">
                            <i class="fas fa-times"></i>
                        </button>` : ''}
                    </div>
                </div>
                <p class="mb-0">${reply.content}</p>
                <div class="mt-2">
                    <button class="btn btn-sm btn-outline-secondary reply-to-reply-btn" data-id="${reply.id}" data-username="${reply.username || 'Anonymous'}">
                        <i class="fas fa-reply me-1"></i> Reply
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Add event listener for delete button
    const deleteReplyBtn = replyElement.querySelector('.delete-reply-btn');
    if (deleteReplyBtn) {
        deleteReplyBtn.addEventListener('click', function() {
            const replyId = this.dataset.id;
            if (confirm('Are you sure you want to delete this reply?')) {
                deleteComment(replyId);
            }
        });
    }
    
    // Add event listener for reply button
    const replyToReplyBtn = replyElement.querySelector('.reply-to-reply-btn');
    if (replyToReplyBtn) {
        replyToReplyBtn.addEventListener('click', function() {
            const replyId = this.dataset.id;
            const username = this.dataset.username;
            showReplyForm(replyId, username, replyElement);
        });
    }
    
    // Add nested replies if any (recursive)
    if (reply.replies && reply.replies.length > 0) {
        const nestedRepliesContainer = document.createElement('div');
        nestedRepliesContainer.classList.add('nested-replies', 'ms-4', 'mt-2');
        
        reply.replies.forEach(nestedReply => {
            renderReply(nestedReply, nestedRepliesContainer);
        });
        
        replyElement.appendChild(nestedRepliesContainer);
    }
    
    container.appendChild(replyElement);
    return replyElement;
}

// Show reply form for a comment or reply
function showReplyForm(commentId, username, commentElement) {
    // Store the original commentId string value
    commentElement.dataset.originalId = commentId;
    
    // Remove any existing reply forms
    document.querySelectorAll('.reply-form-container').forEach(el => el.remove());
    
    // Create reply form
    const replyFormContainer = document.createElement('div');
    replyFormContainer.classList.add('reply-form-container', 'ms-4', 'mt-2');
    
    replyFormContainer.innerHTML = `
        <div class="card">
            <div class="card-body">
                <form class="reply-form">
                    <div class="mb-2">
                        <textarea class="form-control reply-content" rows="2" placeholder="Reply to ${username}..."></textarea>
                    </div>
                    <div class="d-flex justify-content-end">
                        <button type="button" class="btn btn-sm btn-secondary me-2 cancel-reply-btn">Cancel</button>
                        <button type="submit" class="btn btn-sm btn-primary submit-reply-btn">Reply</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    
    // Add event listeners
    const replyForm = replyFormContainer.querySelector('.reply-form');
    const cancelBtn = replyFormContainer.querySelector('.cancel-reply-btn');
    
    cancelBtn.addEventListener('click', function() {
        replyFormContainer.remove();
    });
    
    replyForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const replyContent = replyFormContainer.querySelector('.reply-content').value.trim();
        if (!replyContent) return;
        
        // Submit reply
        submitReply(commentId, replyContent, replyFormContainer);
    });
    
    // Append to comment element
    commentElement.appendChild(replyFormContainer);
    
    // Focus on textarea
    replyFormContainer.querySelector('.reply-content').focus();
}

// Submit a reply to a comment
function submitReply(parentId, content, formContainer) {
    if (!currentUser) {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
        return;
    }
    
    // Try to get the original ID to avoid precision issues
    const commentElement = formContainer.closest('.comment-item');
    const originalParentId = commentElement.dataset.originalId || parentId;
    
    // Create optimistic reply
    const optimisticReply = {
        id: 'temp-reply-' + Date.now(),
        content: content,
        userId: currentUser.sub,
        username: currentUser.username || currentUser.preferred_username || 'You',
        avatarUrl: currentUser.avatarUrl,
        createdAt: new Date().toISOString()
    };
    
    // Get or create replies container
    const repliesContainer = commentElement.querySelector('.replies-container') || 
        (() => {
            const container = document.createElement('div');
            container.classList.add('replies-container', 'ms-4', 'mt-2');
            commentElement.appendChild(container);
            return container;
        })();
    
    // Add optimistic reply to UI
    const replyElement = document.createElement('div');
    replyElement.classList.add('reply-item', 'mb-2', 'reply-pending');
    
    const replyDate = formatDate(optimisticReply.createdAt);
    
    replyElement.innerHTML = `
        <div class="card border-primary">
            <div class="card-body py-2 px-3">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <div class="d-flex align-items-center">
                        <img src="${optimisticReply.avatarUrl || 'https://via.placeholder.com/24'}" class="rounded-circle me-2" width="24" height="24" style="object-fit: cover;" alt="User">
                        <div>
                            <div class="fw-bold">${optimisticReply.username}</div>
                            <div class="text-muted small">
                                ${replyDate}
                                <span class="ms-2 badge bg-primary">Posting...</span>
                            </div>
                        </div>
                    </div>
                </div>
                <p class="mb-0">${optimisticReply.content}</p>
            </div>
        </div>
    `;
    
    repliesContainer.insertBefore(replyElement, repliesContainer.firstChild);
    
    // Remove form
    formContainer.remove();
    
    // Send to server
    AuthUtils.authenticatedFetch('/api/comments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            articleId: articleId,
            content: content,
            parentId: String(originalParentId) // Use string type to preserve full precision
        })
    })
    .then(data => {
        if (data.status.code === 0) {
            // Replace optimistic reply with real one after a delay
            setTimeout(() => {
                loadComments();
            }, 1000);
        } else {
            // Remove optimistic reply and show error
            replyElement.remove();
            
            // Show error toast
            const errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-danger mb-2';
            errorDiv.textContent = 'Failed to post reply: ' + (data.status.msg || 'Unknown error');
            repliesContainer.prepend(errorDiv);
            
            // Remove error after 5 seconds
            setTimeout(() => {
                errorDiv.remove();
            }, 5000);
        }
    })
    .catch(error => {
        console.error('Error posting reply:', error);
        
        // Remove optimistic reply
        replyElement.remove();
        
        // Show error toast
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger mb-2';
        errorDiv.textContent = 'Failed to post reply: ' + error.message;
        repliesContainer.prepend(errorDiv);
        
        // Remove error after 5 seconds
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    });
}

// Delete a comment or reply
function deleteComment(commentId) {
    if (!currentUser) {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
        return;
    }
    
    AuthUtils.authenticatedFetch(`/api/comments/${commentId}`, {
        method: 'DELETE'
    })
    .then(data => {
        if (data.status.code === 0) {
            // Reload comments
            loadComments();
        }
    })
    .catch(error => console.error('Error deleting comment:', error));
}

// Load related articles
function loadRelatedArticles(tags) {
    const relatedArticles = document.getElementById('related-articles');
    if (!relatedArticles) return;
    
    // Clear loading indicator
    relatedArticles.innerHTML = '';
    
    // Add placeholder related articles
    const relatedArticleItems = [
        { id: 1, title: "Introduction to Spring Boot" },
        { id: 2, title: "Working with JPA and Hibernate" },
        { id: 3, title: "Securing REST APIs with JWT" },
        { id: 4, title: "Building Responsive UI with Bootstrap" }
    ];
    
    // Add placeholder items
    relatedArticleItems.forEach(item => {
        const article = document.createElement('a');
        article.classList.add('list-group-item', 'list-group-item-action');
        article.href = `/articles/${item.id}`;
        article.textContent = item.title;
        relatedArticles.appendChild(article);
    });
    
    // Load placeholder popular tags
    const popularTags = document.getElementById('popular-tags');
    if (!popularTags) return;
    
    // Clear loading indicator
    popularTags.innerHTML = '';
    
    // Add placeholder tags
    const tagsList = ['Java', 'Spring', 'DevOps', 'Microservices', 'Security', 'Docker', 'Kubernetes', 'Frontend'];
    tagsList.forEach(tag => {
        const tagElement = document.createElement('a');
        tagElement.classList.add('badge', 'bg-secondary', 'me-1', 'mb-1', 'text-decoration-none');
        tagElement.href = `/articles?tag=${tag}`;
        tagElement.textContent = tag;
        popularTags.appendChild(tagElement);
    });
}

// Helper function to format date
function formatDate(dateString) {
    if (!dateString) return 'Unknown';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Submit a comment
function handleCommentSubmit(e) {
    e.preventDefault();
    
    if (!AuthUtils.isAuthenticated()) {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
        return;
    }
    
    const commentContentEl = document.getElementById('comment-content');
    if (!commentContentEl) return;
    
    const commentContent = commentContentEl.value.trim();
    if (!commentContent) return;
    
    // Clear the form
    commentContentEl.value = '';
    
    // Get current comment count
    const commentCount = document.getElementById('comment-count');
    if (!commentCount) return;
    
    const count = parseInt(commentCount.textContent || '0');
    
    // Update comment count
    commentCount.textContent = count + 1;
    
    // Create optimistic comment
    const optimisticComment = {
        id: 'temp-' + Date.now(),
        articleId: articleId,
        userId: currentUser.sub,
        username: currentUser.username || currentUser.preferred_username || 'You',
        avatarUrl: currentUser.avatarUrl,
        content: commentContent,
        createdAt: new Date().toISOString(),
        replies: []
    };
    
    // Show optimistic comment with pending indicator
    const commentsList = document.getElementById('comments-list');
    if (!commentsList) return;
    
    const noComments = document.getElementById('no-comments');
    if (noComments) {
        noComments.classList.add('d-none');
    }
    
    // Render optimistic comment with pending state
    const commentElement = renderComment(optimisticComment, commentsList, true);
    
    // Submit to server
    AuthUtils.authenticatedFetch('/api/comments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            articleId: articleId,
            content: commentContent,
            parentId: null
        })
    })
    .then(data => {
        if (data.status.code === 0) {
            // Replace optimistic comment with real one after a delay
            // This ensures the comment is processed by the async system
            setTimeout(() => {
                loadComments();
            }, 1000);
        } else {
            // Remove optimistic comment and show error
            commentElement.remove();
            
            // Restore count
            commentCount.textContent = count;
            
            // Show error
            const errorDiv = document.createElement('div');
            errorDiv.className = 'alert alert-danger';
            errorDiv.textContent = 'Failed to post comment: ' + (data.status.msg || 'Unknown error');
            commentsList.prepend(errorDiv);
            
            // Remove error after 5 seconds
            setTimeout(() => {
                errorDiv.remove();
            }, 5000);
        }
    })
    .catch(error => {
        console.error('Error posting comment:', error);
        
        // Remove optimistic comment
        commentElement.remove();
        
        // Restore count
        commentCount.textContent = count;
        
        // Show error
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger';
        errorDiv.textContent = 'Failed to post comment: ' + error.message;
        commentsList.prepend(errorDiv);
        
        // Remove error after 5 seconds
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    });
} 