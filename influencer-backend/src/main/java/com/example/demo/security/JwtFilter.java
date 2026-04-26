package com.example.demo.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.Collections;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Servlet filter that enforces JWT authentication on all protected endpoints.
 * Public paths (login and register) pass through without a token.
 * CORS headers are added here for error responses that bypass Spring's CORS config.
 */
@Component
public class JwtFilter implements Filter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000"
    );

    /** Exact paths that do not require a JWT. */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/auth/register"
    );

    /** Prefix paths that should bypass JWT checks (e.g. SockJS handshake endpoints). */
    private static final Set<String> PUBLIC_PREFIX_PATHS = Set.of(
            "/ws-chat"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Allow CORS preflight requests to pass through immediately
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            addCorsHeaders(req, res);
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = resolvedPath(req);

        // Skip JWT check for whitelisted public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            addCorsHeaders(req, res);
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        String token = header.substring(7).trim();
        String username;
        try {
            username = JwtUtil.extractUsername(token);
        } catch (Exception e) {
            addCorsHeaders(req, res);
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String resolvedPath(HttpServletRequest req) {
        String path = req.getServletPath();
        if (path == null || path.isEmpty()) {
            path = req.getRequestURI();
            String ctx = req.getContextPath();
            if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
                path = path.substring(ctx.length());
            }
        }
        return path;
    }

    private boolean isPublicPath(String path) {
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        for (String prefix : PUBLIC_PREFIX_PATHS) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void addCorsHeaders(HttpServletRequest req, HttpServletResponse res) {
        String origin = req.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
    }
}
