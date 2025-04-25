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
            }
        }
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

    // 初始化页面组件和功能
    initPage();
    setupAvatarUpload();
    setupProfileEdit();
}); 