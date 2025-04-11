package org.jeffrey.core.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 判断当前请求是否为登录/注册/swagger请求，如果是，则放行
        String requestPath = request.getServletPath();
        if (requestPath.contains("/auth") || requestPath.contains("/") || requestPath.contains("/test")
                || requestPath.contains("/swagger-ui") || requestPath.contains("/doc.html")) {
            log.debug("登录/注册/swagger请求{}，放行", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(request.getServletPath());
        // 获取Authorization请求头中的信息
        final String bearerToken = request.getHeader("Authorization");
        // 判断Token是否非法
        if (bearerToken == null || bearerToken.isBlank() || !bearerToken.startsWith("Bearer ")) {
            throw new InsufficientAuthenticationException("没有发现Token，或者Token非法: " + bearerToken);
        }

        final String token = bearerToken.substring(7);     // 去掉token前缀"Bearer "，拿到真实token

        if (!token.isBlank()) {
            try {
                String username = jwtUtil.extractUsername(token);
                // 5. 创建 Authentication 对象 (已认证状态)
                // 初始化 UsernamePasswordAuthenticationToken
                // 注意：这里使用了三个参数的构造函数，因为它会调用 super.setAuthenticated(true);
                // 这表示我们基于有效的 Token 确认了用户的身份。
                // 第一个参数是 principal (通常是用户名或 UserDetails 对象)
                // 第二个参数是 credentials (对于 JWT 认证，通常设为 null)
                // 第三个参数是 authorities (权限列表，这里暂时为空)
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()); // 使用空权限列表
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

}
