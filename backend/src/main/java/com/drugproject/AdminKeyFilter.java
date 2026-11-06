package com.drugproject;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Protects the Admin links (/api/admin/...) with a secret key.
 * - If no key is set (like on your own laptop), everything is open.
 * - Online, set an environment variable called ADMIN_KEY. Then the Admin page must send the same key.
 */
@Component
public class AdminKeyFilter extends OncePerRequestFilter {

    @Value("${admin.key:}")
    private String adminKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/admin") || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        boolean open = adminKey == null || adminKey.isBlank();
        if (open || adminKey.equals(request.getHeader("X-Admin-Key"))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Wrong or missing admin key\"}");
    }
}
