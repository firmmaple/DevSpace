package org.jeffrey.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.http.HttpMethod; // 如果需要按方法匹配

import java.util.List;

@Configuration
public class SecurityMatchersConfig {

    // 定义所有公开访问路径的 Matcher 列表
    private static final List<RequestMatcher> PUBLIC_MATCHERS = List.of(
            new AntPathRequestMatcher("/login"),
            new AntPathRequestMatcher("/register"),
            new AntPathRequestMatcher("/auth/login"),
            new AntPathRequestMatcher("/auth/register"),
            new AntPathRequestMatcher("/"), // 根路径
            new AntPathRequestMatcher("/test/**"), // test及其子路径
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/static/**"),
            new AntPathRequestMatcher("/webjars/**"),
            // Swagger / OpenAPI / Knife4j 文档路径 (根据你的实际使用情况调整)
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-resources/**"),
            new AntPathRequestMatcher("/doc.html"), // Knife4j
            // 其他需要公开访问的路径
            new AntPathRequestMatcher("/articles", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/articles/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/articles/**", HttpMethod.GET.name())
            // ... 添加更多公共路径规则 ...
    );

    /**
     * 创建一个组合的 RequestMatcher Bean，
     * 任何请求只要匹配 PUBLIC_MATCHERS 中的任何一个，这个 Bean 就会匹配成功。
     *
     * @return 一个 OrRequestMatcher 实例
     */
    @Bean
    public RequestMatcher publicEndpointsMatcher() {
        return new OrRequestMatcher(PUBLIC_MATCHERS);
    }

    // 如果你需要单独引用某个 Matcher，也可以把它定义为 Bean
    // @Bean
    // public RequestMatcher loginEndpointMatcher() {
    //     return new AntPathRequestMatcher("/auth/login");
    // }
}