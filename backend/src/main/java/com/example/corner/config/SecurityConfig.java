package com.example.corner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security 配置类 仅用于提供 PasswordEncoder Bean，禁用默认 Web 安全认证
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 禁用默认的安全过滤链，允许所有请求通过 因为我们使用自定义的 JWT 拦截器进行认证
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // 禁用 CSRF（API 项目不需要）
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll() // 允许所有请求通过
                );

        return http.build();
    }
}
