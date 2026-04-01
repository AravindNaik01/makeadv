package com.example.demo;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtFilter implements Filter {

	private static final Set<String> ALLOWED_ORIGINS = Set.of(
			"http://localhost:3000",
			"http://127.0.0.1:3000"
	);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {

	    HttpServletRequest req = (HttpServletRequest) request;
	    HttpServletResponse res = (HttpServletResponse) response;

	    // ✅ Allow CORS preflight to pass through (browser needs 200 + CORS headers)
	    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
	        addCorsHeaders(req, res);
	        res.setStatus(HttpServletResponse.SC_OK);
	        return;
	    }

	    // Path after context path — getRequestURI() can include context path and break matching
	    String path = req.getServletPath();
	    if (path == null || path.isEmpty()) {
	        path = req.getRequestURI();
	        String ctx = req.getContextPath();
	        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
	            path = path.substring(ctx.length());
	        }
	    }

	    // ✅ Skip auth for login, register, Instagram OAuth
	    if ("/login".equals(path)
	            || "/register".equals(path)
	            || "/auth/instagram/url".equals(path)
	            || "/auth/instagram/callback".equals(path)
	            || "/auth/instagram/dev-callback".equals(path)) {
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

	    try {
	        JwtUtil.extractUsername(token);
	    } catch (Exception e) {
	        addCorsHeaders(req, res);
	        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
	        return;
	    }

	    chain.doFilter(request, response);
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