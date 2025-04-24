/**
 * Authentication utility functions for JWT handling
 */
const AuthUtils = {
    /**
     * Store user info in localStorage.
     * Note: JWT token is now stored in HTTP-only cookie by the server.
     * @param {Object} userInfo - User information object
     */
    setUserInfo: function(userInfo) {
        console.log("AuthUtils: Setting user info", userInfo);
        if (!userInfo) {
            console.warn("AuthUtils: Attempting to store null or undefined user info");
            return;
        }
        localStorage.setItem('user_info', JSON.stringify(userInfo));
    },

    /**
     * Get the user info from localStorage
     * @returns {Object|null} The user info object or null if not found
     */
    getUserInfo: function() {
        const userInfoStr = localStorage.getItem('user_info');
        const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null;
        console.log("AuthUtils: Getting user info", userInfo);
        return userInfo;
    },

    /**
     * Remove user info from localStorage
     */
    removeUserInfo: function() {
        console.log("AuthUtils: Removing user info");
        localStorage.removeItem('user_info');
    },

    /**
     * Check if the user is authenticated by checking if user info exists
     * @returns {boolean} True if authenticated, false otherwise
     */
    isAuthenticated: function() {
        return !!this.getUserInfo();
    },

    /**
     * Handle API response
     * @param {Object} response - API response
     * @returns {Promise} Fetch promise
     */
    handleApiResponse: function(response) {
        // 检查状态码
        if (response.status && response.status.code !== 0) {
            // 未登录错误码 (100_403_003)
          if (response.status.code === 100403003) {
            // 保存当前页面 URL，以便登录后返回
            localStorage.setItem('redirectUrl', window.location.href);
            // 重定向到登录页
            window.location.href = '/login';
            return Promise.reject(new Error('用户未登录'));
          }
          
          // 其他错误处理
          return Promise.reject(new Error(response.status.msg));
        }
        
        return response;
    },

    /**
     * Wrapper for fetch that uses cookies for authentication
     * Note: JWT token is automatically sent in cookies for authenticated requests
     * @param {string} url - The URL to fetch
     * @param {Object} options - Fetch request options
     * @returns {Promise} Fetch promise
     */
    authenticatedFetch: function(url, options = {}) {
        // Make sure to include credentials to send cookies
        const requestOptions = {
            ...options,
            credentials: 'include'  // This ensures cookies are sent with the request
        };
        
        return fetch(url, requestOptions)
            .then(res => res.json())
            .then(this.handleApiResponse);
    },

    /**
     * Handle logout
     */
    logout: function() {
        // Call the logout endpoint
        fetch('/auth/logout', {
            method: 'POST',
            credentials: 'include'  // Include cookies in the request
        })
        .finally(() => {
            // Remove user info and redirect regardless of server response
            this.removeUserInfo();
            window.location.href = '/';
        });
    },

    /**
     * Initialize AuthUtils - check if auth data exists and is valid
     * @returns {boolean} True if initialized successfully with valid data
     */
    init: function() {
        try {
            // Check if user info exists and has minimum required fields
            const userInfo = this.getUserInfo();
            if (!userInfo || !userInfo.username) return false;
            
            console.log("AuthUtils initialized successfully");
            return true;
        } catch (error) {
            console.error("Error initializing AuthUtils:", error);
            return false;
        }
    },

    /**
     * Update user information in localStorage after profile changes
     * @param {Object} updatedProfile - Updated user profile information
     */
    updateUserInfo: function(updatedProfile) {
        if (!updatedProfile) return;
        
        const currentUserInfo = this.getUserInfo();
        if (!currentUserInfo) return;
        
        // 更新用户名和其他可能变更的信息
        const newUserInfo = {
            ...currentUserInfo,
            username: updatedProfile.username || currentUserInfo.username,
            email: updatedProfile.email || currentUserInfo.email,
            bio: updatedProfile.bio || currentUserInfo.bio,
            avatarUrl: updatedProfile.avatarUrl || currentUserInfo.avatarUrl
        };
        
        this.setUserInfo(newUserInfo);
        console.log("User info updated in localStorage", newUserInfo);
        
        // 触发自定义事件，通知header更新头像
        const userInfoUpdatedEvent = new CustomEvent('userInfoUpdated', {
            detail: { newUserInfo: newUserInfo }
        });
        document.dispatchEvent(userInfoUpdatedEvent);
        
        // 如果用户名已更改，可能需要刷新页面以更新UI
        if (updatedProfile.username && updatedProfile.username !== currentUserInfo.username) {
            console.log("Username changed, refreshing page to update UI");
            // 可以选择刷新页面或只更新特定UI元素
            // window.location.reload();
        }
    }
};

// Ensure AuthUtils is added to the global window object
window.AuthUtils = AuthUtils;