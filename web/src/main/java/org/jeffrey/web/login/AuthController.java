package org.jeffrey.web.login;

import lombok.RequiredArgsConstructor;
import org.jeffrey.api.vo.ResVo;
import org.jeffrey.api.vo.StatusEnum;
import org.jeffrey.core.security.CustomUserDetails;
import org.jeffrey.core.security.JWTUtil;
import org.jeffrey.core.security.UserInfo;
import org.jeffrey.service.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    public ResVo<Map<String, Object>> authenticateUser(@RequestParam String username, @RequestParam String password) {
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
        
        // 记录认证成功的用户信息
        UserInfo userInfo = userDetails.getUser();
        
        // 创建认证信息返回对象
        Map<String, Object> authInfo = new HashMap<>(2);
        authInfo.put("token", token);
        authInfo.put("user", userInfo);
        
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
    public ResVo<String> logout() {
        // Clear security context
        SecurityContextHolder.clearContext();
        return ResVo.ok("Logged out successfully");
    }
}