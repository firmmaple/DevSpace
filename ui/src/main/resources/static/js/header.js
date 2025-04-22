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
    
    // Check if user is authenticated
    function updateAuthUI() {
        // Directly check localStorage if AuthUtils is not available
        const isAuthenticated = window.AuthUtils ? 
            AuthUtils.isAuthenticated() : 
            !!localStorage.getItem('jwt_token');
            
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
                const userInfoStr = localStorage.getItem('user_info');
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
            
            // Update user initials avatar
            if (userInfo && userInfo.username && userInitialsElement) {
                // Get first letter of username for avatar
                const initials = userInfo.username.charAt(0).toUpperCase();
                userInitialsElement.textContent = initials;
                
                // Set a background color based on username (simple hash for consistent color)
                const hash = simpleHash(userInfo.username);
                const hue = hash % 360; // 0-359 hue value
                userInitialsElement.style.backgroundColor = `hsl(${hue}, 70%, 60%)`;
            }
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
    
    // Update UI whenever the storage changes (in case of login/logout in another tab)
    window.addEventListener('storage', function(e) {
        if (e.key === 'jwt_token' || e.key === 'user_info') {
            updateAuthUI();
        }
    });
}); 