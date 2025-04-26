/**
 * 用户个人资料页面脚本
 */
document.addEventListener('DOMContentLoaded', function() {
    // 检查用户认证状态
    if (!window.AuthUtils || !AuthUtils.isAuthenticated()) {
        window.location.href = '/login';
        return;
    }
    
    // 从本地存储获取用户基本信息
    const userInfo = AuthUtils.getUserInfo();

    // DOM元素 - 个人资料视图
    const profileViewMode = document.getElementById('profileViewMode');
    const profileEditMode = document.getElementById('profileEditMode');
    const editProfileBtn = document.getElementById('editProfileBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    const profileEditForm = document.getElementById('profileEditForm');
    const profileEditError = document.getElementById('profileEditError');
    const profileEditSuccess = document.getElementById('profileEditSuccess');
    const saveSpinner = document.getElementById('saveSpinner');
    
    // 表单元素
    const editUsername = document.getElementById('editUsername');
    const editEmail = document.getElementById('editEmail');
    const editBio = document.getElementById('editBio');
    
    // 头像相关元素
    const profileAvatar = document.getElementById('profileAvatar');
    const avatarHoverOverlay = document.getElementById('avatarHoverOverlay');
    const avatarFileInput = document.getElementById('avatarFileInput');
    
    // 信息显示元素
    const profileUsername = document.getElementById('profileUsername');
    const profileRole = document.getElementById('profileRole');
    const profileInitials = document.getElementById('profileInitials');
    const infoUsername = document.getElementById('infoUsername');
    const infoEmail = document.getElementById('infoEmail');
    const infoBio = document.getElementById('infoBio');
    const infoJoinDate = document.getElementById('infoJoinDate');

    /**
     * 初始化页面内容
     */
    function initPage() {
        if (userInfo && userInfo.username) {
            // 设置用户名
            profileUsername.textContent = userInfo.username;
            infoUsername.textContent = userInfo.username;
            
            // 设置用户角色
            const roleText = userInfo.isAdmin ? 'Administrator' : 'User';
            profileRole.textContent = roleText;
            
            // 设置用户名首字母
            const initials = userInfo.username.charAt(0).toUpperCase();
            profileInitials.textContent = initials;
            
            // 加载用户详细资料
            loadUserProfile();
        }
        
        // 处理URL hash进行标签切换
        const hash = window.location.hash;
        if (hash) {
            const tabLink = document.querySelector(`a[href="${hash}"]`);
            if (tabLink) {
                tabLink.click();
                
                // 如果切换到我的文章或我的收藏标签页，加载相应数据
                if (hash === '#my-articles') {
                    loadMyArticles(1);
                } else if (hash === '#my-collections') {
                    loadMyCollections(1);
                }
            }
        }
        
        // 添加标签页切换事件监听
        const tabLinks = document.querySelectorAll('.list-group-item[data-bs-toggle="list"]');
        tabLinks.forEach(link => {
            link.addEventListener('shown.bs.tab', function(event) {
                const targetId = event.target.getAttribute('href');
                if (targetId === '#my-articles') {
                    loadMyArticles(1);
                } else if (targetId === '#my-collections') {
                    loadMyCollections(1);
                }
            });
        });
    }

    /**
     * 加载用户详细资料
     */
    function loadUserProfile() {
        AuthUtils.authenticatedFetch('/api/user/profile')
            .then(response => {
                if (response.status.code === 0 && response.result) {
                    updateProfileDisplay(response.result);
                }
            })
            .catch(error => {
                console.error('Failed to load user profile:', error);
            });
    }
    
    /**
     * 更新页面显示的用户资料
     */
    function updateProfileDisplay(profile) {
        // 更新显示信息
        profileUsername.textContent = profile.username || userInfo.username;
        infoUsername.textContent = profile.username || userInfo.username;
        infoEmail.textContent = profile.email || 'No email set';
        infoBio.textContent = profile.bio || 'No bio yet';
        
        if (profile.joinDate) {
            const joinDate = new Date(profile.joinDate);
            infoJoinDate.textContent = joinDate.toLocaleDateString();
        }
        
        // 更新头像
        if (profile.avatarUrl) {
            updateAvatarDisplay(profile.avatarUrl);
        }
    }
    
    /**
     * 更新头像显示
     */
    function updateAvatarDisplay(avatarUrl) {
        if (avatarUrl) {
            const avatarElement = document.getElementById('profileAvatar');
            const initials = document.getElementById('profileInitials');
            
            // 检查是否已存在头像图片
            let avatarImg = avatarElement.querySelector('img');
            if (!avatarImg) {
                // 创建新的img元素
                avatarImg = document.createElement('img');
                avatarImg.className = 'w-100 h-100 object-fit-cover';
                avatarImg.alt = 'User Avatar';
                avatarElement.appendChild(avatarImg);
            }
            
            // 设置头像图片和隐藏文字
            avatarImg.src = avatarUrl;
            avatarImg.style.display = 'block';
            if (initials) {
                initials.style.display = 'none';
            }
        }
    }

    /**
     * 设置头像上传事件处理
     */
    function setupAvatarUpload() {
        if (profileAvatar) {
            // 头像悬停效果
            profileAvatar.addEventListener('mouseenter', function() {
                if (avatarHoverOverlay) {
                    avatarHoverOverlay.style.display = 'flex';
                }
            });
            
            profileAvatar.addEventListener('mouseleave', function() {
                if (avatarHoverOverlay) {
                    avatarHoverOverlay.style.display = 'none';
                }
            });
            
            // 点击头像触发文件选择
            profileAvatar.addEventListener('click', function() {
                if (avatarFileInput) {
                    avatarFileInput.click();
                }
            });
        }
        
        // 处理头像文件选择
        if (avatarFileInput) {
            avatarFileInput.addEventListener('change', function() {
                if (avatarFileInput.files.length > 0) {
                    const avatarFile = avatarFileInput.files[0];
                    
                    // 检查文件类型
                    if (!avatarFile.type.match('image/jpeg') && !avatarFile.type.match('image/png')) {
                        alert('只支持JPG和PNG格式的图片');
                        return;
                    }
                    
                    // 检查文件大小（最大2MB）
                    if (avatarFile.size > 2 * 1024 * 1024) {
                        alert('图片大小不能超过2MB');
                        return;
                    }
                    
                    // 显示上传中状态
                    const uploadingIndicator = document.createElement('div');
                    uploadingIndicator.className = 'position-absolute w-100 h-100 d-flex align-items-center justify-content-center bg-dark bg-opacity-50';
                    uploadingIndicator.innerHTML = '<span class="spinner-border spinner-border-sm text-white"></span>';
                    profileAvatar.appendChild(uploadingIndicator);
                    
                    // 准备头像上传数据
                    const avatarFormData = new FormData();
                    avatarFormData.append('avatar', avatarFile);
                    
                    // 发送头像上传请求
                    AuthUtils.authenticatedFetch('/api/user/avatar', {
                        method: 'POST',
                        body: avatarFormData
                    })
                    .then(response => {
                        // 移除上传中状态
                        profileAvatar.removeChild(uploadingIndicator);
                        
                        if (response.status.code === 0) {
                            // 上传成功，更新头像显示
                            const avatarUrl = response.result;
                            updateAvatarDisplay(avatarUrl);
                            
                            // 更新本地用户信息
                            const currentUserInfo = AuthUtils.getUserInfo();
                            if (currentUserInfo) {
                                currentUserInfo.avatarUrl = avatarUrl;
                                AuthUtils.setUserInfo(currentUserInfo);
                                
                                // 触发自定义事件，通知header更新头像
                                const userInfoUpdatedEvent = new CustomEvent('userInfoUpdated', {
                                    detail: { newUserInfo: currentUserInfo }
                                });
                                document.dispatchEvent(userInfoUpdatedEvent);
                            }
                        } else {
                            // 上传失败
                            alert('头像上传失败: ' + response.status.msg);
                        }
                    })
                    .catch(error => {
                        // 移除上传中状态
                        profileAvatar.removeChild(uploadingIndicator);
                        alert('头像上传失败: ' + error.message);
                        console.error('头像上传错误:', error);
                    });
                }
            });
        }
    }

    /**
     * 设置个人资料编辑事件处理
     */
    function setupProfileEdit() {
        // 添加编辑按钮点击事件
        if (editProfileBtn) {
            editProfileBtn.addEventListener('click', function() {
                profileViewMode.style.display = 'none';
                profileEditMode.style.display = 'block';
                
                // 隐藏任何之前的成功或错误消息
                profileEditError.classList.add('d-none');
                profileEditSuccess.classList.add('d-none');
                
                // 先获取最新的用户资料，确保Bio等信息是最新的
                AuthUtils.authenticatedFetch('/api/user/profile')
                .then(response => {
                    if (response.status.code === 0 && response.result) {
                        const profile = response.result;
                        
                        // 填充表单数据
                        editUsername.value = profile.username || userInfo.username || '';
                        editEmail.value = profile.email || userInfo.email || '';
                        editBio.value = profile.bio || '';
                        
                        // 更新本地存储的用户信息
                        AuthUtils.updateUserInfo(profile);
                    } else {
                        // 如果获取失败，则使用本地缓存的信息
                        editUsername.value = userInfo.username || '';
                        editEmail.value = userInfo.email || '';
                        editBio.value = userInfo.bio || '';
                    }
                })
                .catch(error => {
                    // 出错时使用本地缓存信息
                    console.error('获取用户资料失败:', error);
                    editUsername.value = userInfo.username || '';
                    editEmail.value = userInfo.email || '';
                    editBio.value = userInfo.bio || '';
                });
            });
        }
        
        // 添加取消按钮点击事件
        if (cancelEditBtn) {
            cancelEditBtn.addEventListener('click', function() {
                profileViewMode.style.display = 'block';
                profileEditMode.style.display = 'none';
                profileEditError.classList.add('d-none');
                profileEditSuccess.classList.add('d-none');
                profileEditForm.reset();
            });
        }
        
        // 表单提交事件
        if (profileEditForm) {
            profileEditForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                // 表单验证
                if (!profileEditForm.checkValidity()) {
                    e.stopPropagation();
                    profileEditForm.classList.add('was-validated');
                    return;
                }
                
                // 隐藏错误消息
                profileEditError.classList.add('d-none');
                profileEditSuccess.classList.add('d-none');
                
                // 显示加载状态
                saveSpinner.classList.remove('d-none');
                
                // 普通表单数据使用JSON格式提交
                const profileData = {
                    username: editUsername.value,
                    email: editEmail.value,
                    bio: editBio.value
                };
                
                AuthUtils.authenticatedFetch('/api/user/profile', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(profileData)
                })
                .then(response => {
                    saveSpinner.classList.add('d-none');
                    
                    if (response.status.code === 0) {
                        // 更新成功
                        profileEditSuccess.textContent = 'Profile updated successfully';
                        profileEditSuccess.classList.remove('d-none');
                        
                        // 更新页面上的信息
                        const updatedProfile = response.result;
                        updateProfileDisplay(updatedProfile);
                        
                        // 使用新的方法更新本地存储的用户信息
                        AuthUtils.updateUserInfo(updatedProfile);
                        
                        // 延迟返回查看模式
                        setTimeout(() => {
                            profileViewMode.style.display = 'block';
                            profileEditMode.style.display = 'none';
                            profileEditForm.reset();
                        }, 1500);
                    } else {
                        // 更新失败
                        profileEditError.textContent = response.status.msg || 'Failed to update profile';
                        profileEditError.classList.remove('d-none');
                    }
                })
                .catch(error => {
                    saveSpinner.classList.add('d-none');
                    profileEditError.textContent = 'Failed to update profile: ' + error.message;
                    profileEditError.classList.remove('d-none');
                    console.error('Profile update error:', error);
                });
            });
        }
    }

    /**
     * 加载我的文章
     * @param {number} page - 页码
     */
    function loadMyArticles(page) {
        const articlesContainer = document.getElementById('my-articles-container');
        const articlesLoading = document.getElementById('articles-loading');
        const articlesList = document.getElementById('articles-list');
        const noArticles = document.getElementById('no-articles');
        const pagination = document.getElementById('articles-pagination');
        
        if (!articlesContainer || !articlesLoading || !articlesList || !noArticles || !pagination) return;
        
        // 显示加载状态
        articlesLoading.classList.remove('d-none');
        articlesList.classList.add('d-none');
        noArticles.classList.add('d-none');
        
        // 当前用户ID
        const userId = userInfo ? userInfo.id : null;
        if (!userId) return;
        
        // 构建API URL，获取当前用户的文章
        const url = `/api/articles?pageNum=${page}&pageSize=5&authorId=${userId}`;
        
        AuthUtils.authenticatedFetch(url)
            .then(response => {
                articlesLoading.classList.add('d-none');
                
                if (response.status.code === 0 && response.result) {
                    const articles = response.result.records;
                    
                    if (articles && articles.length > 0) {
                        // 显示文章列表
                        articlesList.innerHTML = '';  // 清空现有内容
                        articlesList.classList.remove('d-none');
                        
                        // 渲染文章列表
                        articles.forEach(article => {
                            const articleElement = createArticleElement(article);
                            articlesList.appendChild(articleElement);
                        });
                        
                        // 更新分页
                        updatePagination(response.result, pagination, loadMyArticles);
                    } else {
                        // 没有文章
                        noArticles.classList.remove('d-none');
                        pagination.innerHTML = '';
                    }
                } else {
                    // API返回错误
                    noArticles.textContent = '加载文章失败，请稍后再试';
                    noArticles.classList.remove('d-none');
                    pagination.innerHTML = '';
                }
            })
            .catch(error => {
                articlesLoading.classList.add('d-none');
                noArticles.textContent = '加载文章失败：' + error.message;
                noArticles.classList.remove('d-none');
                pagination.innerHTML = '';
                console.error('加载我的文章失败:', error);
            });
    }

    /**
     * 加载我的收藏
     * @param {number} page - 页码
     */
    function loadMyCollections(page) {
        const collectionsContainer = document.getElementById('my-collections-container');
        const collectionsLoading = document.getElementById('collections-loading');
        const collectionsList = document.getElementById('collections-list');
        const noFavorites = document.getElementById('no-collections');
        const pagination = document.getElementById('collections-pagination');
        
        if (!collectionsContainer || !collectionsLoading || !collectionsList || !noFavorites || !pagination) return;
        
        // 显示加载状态
        collectionsLoading.classList.remove('d-none');
        collectionsList.classList.add('d-none');
        noFavorites.classList.add('d-none');
        
        // 构建API URL，获取当前用户的收藏
        const url = `/api/user/collections?pageNum=${page}&pageSize=5`;
        
        AuthUtils.authenticatedFetch(url)
            .then(response => {
                collectionsLoading.classList.add('d-none');
                
                if (response.status.code === 0 && response.result) {
                    const collections = response.result.records;
                    
                    if (collections && collections.length > 0) {
                        // 显示收藏列表
                        collectionsList.innerHTML = '';  // 清空现有内容
                        collectionsList.classList.remove('d-none');
                        
                        // 渲染收藏列表
                        collections.forEach(article => {
                            const articleElement = createArticleElement(article);
                            collectionsList.appendChild(articleElement);
                        });
                        
                        // 更新分页
                        updatePagination(response.result, pagination, loadMyCollections);
                    } else {
                        // 没有收藏
                        noFavorites.classList.remove('d-none');
                        pagination.innerHTML = '';
                    }
                } else {
                    // API返回错误
                    noFavorites.textContent = '加载收藏失败，请稍后再试';
                    noFavorites.classList.remove('d-none');
                    pagination.innerHTML = '';
                }
            })
            .catch(error => {
                collectionsLoading.classList.add('d-none');
                noFavorites.textContent = '加载收藏失败：' + error.message;
                noFavorites.classList.remove('d-none');
                pagination.innerHTML = '';
                console.error('加载我的收藏失败:', error);
            });
    }

    /**
     * 创建文章元素
     * @param {Object} article - 文章对象
     * @returns {HTMLElement} 文章DOM元素
     */
    function createArticleElement(article) {
        const articleCard = document.createElement('div');
        articleCard.className = 'card mb-3';
        
        const articleBody = document.createElement('div');
        articleBody.className = 'card-body';
        
        // 文章标题
        const titleLink = document.createElement('a');
        titleLink.href = `/articles/${article.id}`;
        titleLink.className = 'text-decoration-none';
        
        const title = document.createElement('h5');
        title.className = 'card-title';
        title.textContent = article.title;
        
        titleLink.appendChild(title);
        articleBody.appendChild(titleLink);
        
        // 文章摘要
        const summary = document.createElement('p');
        summary.className = 'card-text text-muted';
        summary.textContent = article.summary || '无摘要';
        articleBody.appendChild(summary);
        
        // 文章信息（日期和统计）
        const infoRow = document.createElement('div');
        infoRow.className = 'd-flex justify-content-between align-items-center';
        
        const date = document.createElement('small');
        date.className = 'text-muted';
        date.innerHTML = `<i class="far fa-calendar me-1"></i>${formatDate(article.createdAt)}`;
        
        const stats = document.createElement('div');
        stats.innerHTML = `
            <span class="badge bg-primary me-1"><i class="fas fa-eye me-1"></i>${article.viewCount || 0}</span>
            <span class="badge bg-danger me-1"><i class="fas fa-heart me-1"></i>${article.likeCount || 0}</span>
            <span class="badge bg-warning"><i class="fas fa-bookmark me-1"></i>${article.collectCount || 0}</span>
        `;
        
        infoRow.appendChild(date);
        infoRow.appendChild(stats);
        articleBody.appendChild(infoRow);
        
        articleCard.appendChild(articleBody);
        return articleCard;
    }

    /**
     * 更新分页
     * @param {Object} pageData - 分页数据
     * @param {HTMLElement} paginationElement - 分页容器元素
     * @param {Function} loadFunction - 加载函数
     */
    function updatePagination(pageData, paginationElement, loadFunction) {
        paginationElement.innerHTML = '';
        
        const total = pageData.total;
        const pages = pageData.pages;
        const current = pageData.current;
        
        if (pages <= 1) return;  // 只有一页不显示分页
        
        // 上一页
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
                loadFunction(current - 1);
            });
        }
        
        prevLi.appendChild(prevLink);
        paginationElement.appendChild(prevLi);
        
        // 页码
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
                    loadFunction(i);
                });
            }
            
            pageLi.appendChild(pageLink);
            paginationElement.appendChild(pageLi);
        }
        
        // 下一页
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
                loadFunction(current + 1);
            });
        }
        
        nextLi.appendChild(nextLink);
        paginationElement.appendChild(nextLi);
    }

    /**
     * 格式化日期
     * @param {string} dateString - 日期字符串
     * @returns {string} 格式化的日期字符串
     */
    function formatDate(dateString) {
        if (!dateString) return '未知日期';
        
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-CN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    // 初始化页面组件和功能
    initPage();
    setupAvatarUpload();
    setupProfileEdit();
});