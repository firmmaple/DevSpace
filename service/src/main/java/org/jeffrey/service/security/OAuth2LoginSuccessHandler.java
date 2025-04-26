package org.jeffrey.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.api.dto.user.UserDTO;
import org.jeffrey.core.security.JWTUtil;
import org.jeffrey.service.user.repository.entity.UserDO;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final OnlineUserService onlineUserService;
    private final ObjectMapper objectMapper;
    
    // Cookie name constant for the JWT token
    private static final String JWT_COOKIE_NAME = "jwt_token";
    // Cookie name for user info
    private static final String USER_INFO_COOKIE_NAME = "user_info";
    // Cookie max age in seconds (e.g., 1 day)
    private static final int COOKIE_MAX_AGE = 86400;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            
            // getName返回GitHub用户的id
            log.info("OAuth2 login success for user: {}", oAuth2User.getName());
            
            // Extract user information from GitHub
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String githubId = attributes.get("id").toString();
            String username = (String) attributes.get("login"); // GitHub username
            String email = (String) attributes.get("email");
            String avatarUrl = (String) attributes.get("avatar_url");
            
            // Check if the user already exists in our system or create a new one
            UserDO user = userService.processOAuth2User(username, githubId, email, avatarUrl);
            
            // Create a CustomUserDetails object from the user
            CustomUserDetails userDetails = new CustomUserDetails(user);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);
            onlineUserService.save(userDetails.getUsername(), token);
            
            // Set JWT token as HTTP-only cookie
            Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(COOKIE_MAX_AGE);
            response.addCookie(jwtCookie);
            
            // Create UserDTO and set it as a non-HTTP-only cookie so frontend can access it
            UserDTO userDTO = userDetails.toUserDTO();
            String userInfoJson = URLEncoder.encode(objectMapper.writeValueAsString(userDTO), StandardCharsets.UTF_8);
            Cookie userInfoCookie = new Cookie(USER_INFO_COOKIE_NAME, userInfoJson);
            userInfoCookie.setHttpOnly(false); // Allow JavaScript access
            userInfoCookie.setPath("/");
            userInfoCookie.setMaxAge(COOKIE_MAX_AGE);
            response.addCookie(userInfoCookie);
            
            // Redirect to home page
            getRedirectStrategy().sendRedirect(request, response, "/");
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
} 