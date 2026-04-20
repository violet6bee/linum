package ru.shvalieva.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/api")) {
            chain.doFilter(request, response);
            return;
        }

        if (path.equals("/api/auth/login") || path.equals("/api/v1/data")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("admin", null, Collections.emptyList())
                );
                chain.doFilter(request, response);
                return;
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Необходима аутентификация");
    }

    private boolean isPublicPath(String path) {
        return path.equals("/") ||
                path.equals("/index.html") ||
                path.startsWith("/assets/") ||
                path.endsWith(".js") ||
                path.endsWith(".css") ||
                path.endsWith(".png") ||
                path.endsWith(".ico") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/v1/data");
    }
}