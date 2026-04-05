package com.example.lab3.filter;

import com.example.lab3.Service.UserDetailsServiceImpl;
import com.example.lab3.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsServiceImpl userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " → 7 символов

            try {
                // Извлекаем username из токена
                String username = jwtService.getUsernameFromToken(token);

                // Проверяем, что пользователь не аутентифицирован
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Загружаем пользователя (и его роли!) через UserService
                    UserDetails userDetails = userService.loadUserByUsername(username);

                    // Проверяем валидность токена
                    if (jwtService.validateToken(token, userDetails)) {
                        // Создаём Authentication объект
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        // Кладём в контекст безопасности
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Логируем ошибку (опционально)
                System.out.println("JWT validation failed: " + e.getMessage());
            }
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}