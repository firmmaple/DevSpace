package org.jeffrey.service.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeffrey.core.security.JWTUtil;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final RequestMatcher publicEndpointsMatcher;
    private final OnlineUserService onlineUserService;
    
    // Cookie name for JWT token
    private static final String JWT_COOKIE_NAME = "jwt_token";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 首先检查请求是否匹配公共路径
        if (publicEndpointsMatcher.matches(request)) {
            log.info("Request matches public path: {}, skipping JWT Authentication.", request.getServletPath());
            filterChain.doFilter(request, response); // 直接交给下一个过滤器
            return; // 结束当前过滤器的执行
        }

        log.trace("Request does not match public path: {}, proceeding with JWT check.", request.getServletPath());

        // 2. 如果不是公共路径，执行 JWT 认证逻辑
        // 从Cookie中提取JWT令牌
        String token = extractTokenFromCookies(request);

        
        // 判断Token是否存在
        if (token == null) {
            log.warn("JWT cookie is missing or invalid for path: {}", request.getServletPath());
            // 对于非公共路径，没有有效 Token 通常意味着认证失败，让后续流程处理（可能由 ExceptionTranslationFilter 触发 AuthenticationEntryPoint）
            // throw new InsufficientAuthenticationException("没有发现Token，或者Token非法: " + bearerToken);
            filterChain.doFilter(request, response);
            return;
        }

        if (!token.isBlank()) {
            try {
                String username = jwtUtil.extractUsername(token);
                String tokenFromRedis = onlineUserService.findByUsername(username);
                // 检查用户是否被注销
                if(tokenFromRedis == null) {
                    log.warn("Token not found in Redis for user: {}", username);
                    throw new InsufficientAuthenticationException("Token不存在或已过期");
                }

                // 5. 创建 Authentication 对象 (已认证状态)
                // 初始化 UsernamePasswordAuthenticationToken
                // 注意：这里使用了三个参数的构造函数，因为它会调用 super.setAuthenticated(true);
                // 这表示我们基于有效的 Token 确认了用户的身份。
                // 第一个参数是 principal (通常是用户名或 UserDetails 对象)
                // 第二个参数是 credentials (对于 JWT 认证，通常设为 null)
                // 第三个参数是 authorities (权限列表，这里暂时为空)
                // Principal principal = CustomUserDetails()
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // 使用空权限列表
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (MalformedJwtException e) {
                throw new InsufficientAuthenticationException("Token被篡改");
//            } catch (SignatureException e) {
//                throw new InsufficientAuthenticationException("Token签名异常");
            } catch (ExpiredJwtException e) {
                throw new InsufficientAuthenticationException("Token过期");
            } catch (UnsupportedJwtException e) {
                throw new InsufficientAuthenticationException("不支持的Token");
            } catch (Exception e) {
                e.printStackTrace();
                throw new InsufficientAuthenticationException("其他认证异常：" + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求的Cookie中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌，如果不存在则返回null
     */
    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        return Arrays.stream(cookies)
                .filter(cookie -> JWT_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
