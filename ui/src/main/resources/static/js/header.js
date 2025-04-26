/**
 * Header menu functionality
 * Handles user menu display based on authentication status
 */
document.addEventListener('DOMContentLoaded', function() {
    const userMenu = document.getElementById('userMenu');
    const authButtons = document.getElementById('authButtons');
    const usernameElement = document.getElementById('username');
    const userInitialsElement = document.getElementById('userInitials');
    const logoutButton = document.getElementById('logoutButton');
    const adminNavLink = document.getElementById('adminNavLink');
    
    // Debug - check if AuthUtils is loaded
    console.log("Header.js - AuthUtils available:", window.AuthUtils ? "Yes" : "No");
    
    // Helper function to get cookie by name
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
    
    // Check if user is authenticated
    function updateAuthUI() {
        // Use AuthUtils if available, otherwise check cookie directly
        const isAuthenticated = window.AuthUtils ? 
            AuthUtils.isAuthenticated() : 
            !!getCookie('user_info');
            
        console.log("Header updateAuthUI - Auth status:", isAuthenticated);    
        
        if (isAuthenticated) {
            // User is authenticated - show user menu
            if (userMenu) {
                userMenu.classList.add('force-show');
                userMenu.classList.remove('force-hide');
            }
            if (authButtons) {
                authButtons.classList.add('force-hide');
                authButtons.classList.remove('force-show');
            }
            
            // Get user info
            let userInfo;
            if (window.AuthUtils) {
                userInfo = AuthUtils.getUserInfo();
            } else {
                const userInfoStr = getCookie('user_info');
                userInfo = userInfoStr ? JSON.parse(userInfoStr) : null;
            }
            
            // Log user info for debugging
            console.log("Header - User info:", userInfo);
            
            // Update username display
            if (userInfo && userInfo.username && usernameElement) {
                usernameElement.textContent = userInfo.username;
            }
            
            // Show admin link only if user is admin
            if (userInfo && userInfo.isAdmin === true && adminNavLink) {
                adminNavLink.classList.add('force-show');
                adminNavLink.classList.remove('force-hide');
                console.log("Admin link shown for admin user:", userInfo.username);
            } else if (adminNavLink) {
                adminNavLink.classList.add('force-hide');
                adminNavLink.classList.remove('force-show');
            }
            
            // Update user avatar
            updateUserAvatar(userInfo);
        } else {
            // User is not authenticated - show auth buttons
            if (userMenu) {
                userMenu.classList.add('force-hide');
                userMenu.classList.remove('force-show');
            }
            if (authButtons) {
                authButtons.classList.add('force-show');
                authButtons.classList.remove('force-hide');
            }
        }
    }
    
    // Update user avatar based on user info
    function updateUserAvatar(userInfo) {
        console.log("updateUserAvatar - userInfo:", userInfo);
        if (userInfo && userInitialsElement) {
            if (userInfo.avatarUrl) {
                // 用户有头像，显示头像图片
                console.log("Setting user avatar:", userInfo.avatarUrl);
                
                // 检查是否已存在头像图片
                let avatarImg = userInitialsElement.querySelector('img');
                if (!avatarImg) {
                    // 创建新的img元素
                    avatarImg = document.createElement('img');
                    avatarImg.className = 'w-100 h-100 rounded-circle';
                    avatarImg.alt = 'User Avatar';
                    avatarImg.style.objectFit = 'cover';
                    
                    // 清空内容并添加图片
                    userInitialsElement.textContent = '';
                    userInitialsElement.appendChild(avatarImg);
                }
                
                // 设置头像图片
                avatarImg.src = userInfo.avatarUrl;
                // 重置背景色为透明
                userInitialsElement.style.backgroundColor = 'transparent';
            } else {
                // 没有头像，显示用户名首字母
                const initials = userInfo.username.charAt(0).toUpperCase();
                userInitialsElement.textContent = initials;
                
                // Set a background color based on username (simple hash for consistent color)
                const hash = simpleHash(userInfo.username);
                const hue = hash % 360; // 0-359 hue value
                userInitialsElement.style.backgroundColor = `hsl(${hue}, 70%, 60%)`;
            }
        }
    }
    
    // Generate a simple hash from a string
    function simpleHash(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = ((hash << 5) - hash) + str.charCodeAt(i);
            hash = hash & hash; // Convert to 32bit integer
        }
        return Math.abs(hash);
    }
    
    // Handle logout button click
    if (logoutButton) {
        logoutButton.addEventListener('click', function(e) {
            e.preventDefault();
            if (window.AuthUtils) {
                AuthUtils.logout();
            } 
        });
    }
    
    // Initial UI update
    updateAuthUI();
    
    // Monitor cookie changes by periodically checking
    // (cookies don't trigger storage events like localStorage)
    // setInterval(updateAuthUI, 5000);
    
    // 监听自定义事件，当用户信息更新时更新头像
    document.addEventListener('userInfoUpdated', function(e) {
        console.log('userInfoUpdated event received', e.detail);
        if (e.detail && e.detail.newUserInfo) {
            updateUserAvatar(e.detail.newUserInfo);
        }
    });
}); 