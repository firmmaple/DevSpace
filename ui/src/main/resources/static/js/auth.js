/**
 * Authentication utility functions for JWT handling
 */
const AuthUtils = {
    /**
     * Store the JWT token in localStorage
     * @param {string} token - JWT token to store
     */
    setToken: function(token) {
        console.log("AuthUtils: Setting token", token ? "Token exists" : "No token");
        localStorage.setItem('jwt_token', token);
    },

    /**
     * Get the JWT token from localStorage
     * @returns {string|null} The JWT token or null if not found
     */
    getToken: function() {
        const token = localStorage.getItem('jwt_token');
        return token;
    },

    /**
     * Remove the JWT token from localStorage
     */
    removeToken: function() {
        console.log("AuthUtils: Removing tokens");
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_info');
    },

    /**
     * Store the user info in localStorage
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
     * Check if the user is authenticated (has a token)
     * @returns {boolean} True if authenticated, false otherwise
     */
    isAuthenticated: function() {
        return !!this.getToken();
    },

    /**
     * Add the JWT token to fetch request options
     * @param {Object} options - Fetch request options
     * @returns {Object} Updated options with Authorization header
     */
    addTokenToRequest: function(options = {}) {
        const token = this.getToken();
        if (!token) return options;
        
        if (!options.headers) {
            options.headers = {};
        }
        console.log("AuthUtils: Adding token to request", token);
        options.headers['Authorization'] = `Bearer ${token}`;
        return options;
    },

    /**
     * Wrapper for fetch that automatically adds the JWT token
     * @param {string} url - The URL to fetch
     * @param {Object} options - Fetch request options
     * @returns {Promise} Fetch promise
     */
    authenticatedFetch: function(url, options = {}) {
        const requestOptions = this.addTokenToRequest(options);
        return fetch(url, requestOptions);
    },

    /**
     * Handle logout
     */
    logout: function() {
        // Call the logout endpoint
        fetch('/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${this.getToken()}`
            }
        })
        .finally(() => {
            // Remove token and redirect regardless of server response
            this.removeToken();
            window.location.href = '/';
        });
    },

    /**
     * Initialize AuthUtils - check if auth data exists and is valid
     * @returns {boolean} True if initialized successfully with valid data
     */
    init: function() {
        try {
            // Check if token exists
            const token = this.getToken();
            if (!token) return false;
            
            // Check if user info exists and has minimum required fields
            const userInfo = this.getUserInfo();
            if (!userInfo || !userInfo.username) return false;
            
            console.log("AuthUtils initialized successfully");
            return true;
        } catch (error) {
            console.error("Error initializing AuthUtils:", error);
            return false;
        }
    }
};

// Ensure AuthUtils is added to the global window object
window.AuthUtils = AuthUtils;