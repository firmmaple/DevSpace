package org.jeffrey.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Add constructor injection
public class SecurityConfig {
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
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
                        // Permit access to static resources (CSS, JS, images)
                        .requestMatchers("/static/**", "/images/**", "/css/**", "/js/**", "/webjars/**").permitAll()
                        // Permit access to the login page and login processing URL
                        .requestMatchers("/login", "/auth/login").permitAll()
                        // Permit access to registration and registration processing URL
                        .requestMatchers("/register", "/auth/register").permitAll()
                        // Permit access to the home page for now (adjust as needed)
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/swagger-ui/**", "/doc.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/test/**").permitAll()
                        // Require authentication for any other request
                        .anyRequest().permitAll())
//                        .anyRequest().authenticated())
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
        ;

        return http.build();
    }
}
