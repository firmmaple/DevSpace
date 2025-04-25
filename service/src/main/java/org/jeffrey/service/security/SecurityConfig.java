package org.jeffrey.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor // Add constructor injection
public class SecurityConfig {
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final RequestMatcher publicEndpointsMatcher;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    // No explicit UserDetailsService needed here if UserServiceImpl implements it and is a @Service


    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码加密方式
//        return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        // 设置hideUserNotFoundExceptions为false
        // 当hideUserNotFoundExceptions为true的时候，UsernameNotFoundException就会被转换成BadCredentialsException异常
        // 而hideUserNotFoundExceptions的默认值是true
        auth.setHideUserNotFoundExceptions(false);
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());     // 设置password encoder - bcrypt
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicEndpointsMatcher).permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Require authentication for any other request
                        .anyRequest().authenticated())
//                .formLogin(form -> form
//                        // Specify the custom login page URL
//                        .loginPage("/login")
//                        // Specify the URL to submit the username and password
//                        .loginProcessingUrl("/auth/login") // This should match the form action
//                        // Specify the URL to redirect to upon successful login
//                        .defaultSuccessUrl("/", true) // Redirect to home page after login
//                        // Specify the URL to redirect to upon failed login
//                        .failureUrl("/login?error=true") // Add error parameter to login page
//                        .permitAll() // Allow everyone to access the login page and processing URL
//                ).logout(logout -> logout
//                        // Specify the URL to trigger logout
//                        .logoutUrl("/logout")
//                        // Specify the URL to redirect to after logout
//                        .logoutSuccessUrl("/login?logout=true") // Redirect to login page with logout message
//                        .invalidateHttpSession(true) // Invalidate session
//                        .deleteCookies("JSESSIONID") // Delete cookies
//                        .permitAll())
                // Disable CSRF for simplicity in this example, consider enabling it with proper token handling in production
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(jwtAuthenticationFilter, ExceptionTranslationFilter.class)
                .exceptionHandling(
                        httpSecurityExceptionHandlingConfigurer ->
                                httpSecurityExceptionHandlingConfigurer
                                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                                        .accessDeniedHandler(customAccessDeniedHandler));

        return http.build();
    }
}
