package org.jeffrey.web.login;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.service.security.CustomUserDetails;
import org.jeffrey.core.security.JWTUtil;
import org.jeffrey.api.dto.user.UserDTO;
import org.jeffrey.core.trace.TraceLog;
import org.jeffrey.service.security.OnlineUserService;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final OnlineUserService onlineUserService;
    
    // Cookie name constant for the JWT token
    private static final String JWT_COOKIE_NAME = "jwt_token";
    // Cookie max age in seconds (e.g., 1 day)
    private static final int COOKIE_MAX_AGE = 86400;

    /**
     * Renders the login page.
     *
     * @param model  The Spring UI Model.
     * @param error  Indicates if a login error occurred.
     * @param logout Indicates if the user just logged out.
     * @return The name of the login view template.
     */
    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout) {

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }

        model.addAttribute("title", "DevSpace - Login");
        model.addAttribute("currentPage", "login");
        model.addAttribute("viewName", "login");
        
        // Return the login page directly, not via main layout
        return "login";
    }
    
    /**
     * Renders the registration page.
     *
     * @param model The Spring UI Model.
     * @return The name of the register view template.
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("title", "DevSpace - Register");
        model.addAttribute("currentPage", "register");
        model.addAttribute("viewName", "register");
        
        // Return the register page directly, not via main layout
        return "register";
    }

    @PostMapping("/auth/login")
    @ResponseBody
    @TraceLog("用户登录")
    public ResVo<Map<String, Object>> authenticateUser(
            @RequestParam String username, 
            @RequestParam String password,
            HttpServletResponse response) {

        // 构造认证token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                username, password
        );

        // 进行认证
        authenticationToken = (UsernamePasswordAuthenticationToken) authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 获取用户详情并生成token
        CustomUserDetails userDetails = (CustomUserDetails) authenticationToken.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        onlineUserService.save(userDetails.getUsername(), token);
        
        // Set JWT token as an HTTP-only cookie
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(COOKIE_MAX_AGE);
        // In production, use this for HTTPS:
        // jwtCookie.setSecure(true); 
        response.addCookie(jwtCookie);
        
        // 记录认证成功的用户信息
        UserDTO userDTO = userDetails.toUserDTO();

        // 创建认证信息返回对象 (不再返回token，只返回用户信息)
        Map<String, Object> authInfo = new HashMap<>(1);
        authInfo.put("user", userDTO);
        
        return ResVo.ok(authInfo);
    }
    
    /**
     * REST endpoint for user registration
     * 
     * @param username username
     * @param password password
     * @return Success response if registration was successful, error response otherwise
     */
    @PostMapping("/auth/register")
    @ResponseBody
    @TraceLog("用户注册")
    public ResVo<String> registerUser(@RequestParam String username, @RequestParam String password) {
        // Validate username and password
        if (username == null || username.trim().isEmpty() || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            return ResVo.fail(StatusEnum.REGISTER_FAILED_MIXED, "Invalid username format");
        }
        
        if (password == null || password.length() < 6) {
            return ResVo.fail(StatusEnum.REGISTER_FAILED_MIXED, "密码必须至少6个字符");
        }
        
        // Register user
        boolean success = userService.registerUser(username, password);
        
        if (success) {
            return ResVo.ok("User registered successfully");
        } else {
            return ResVo.fail(StatusEnum.REGISTER_USER_EXISTS, username);
        }
    }

    /**
     * REST endpoint for logout
     * 
     * @return Success response
     */
    @PostMapping("/auth/logout")
    @ResponseBody
    public ResVo<String> logout(HttpServletResponse response) {
        // Clear security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            onlineUserService.logout(userDetails.getUsername());
        }
        
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);
        
        SecurityContextHolder.clearContext();
        return ResVo.ok("Logged out successfully");
    }
}