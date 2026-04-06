package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@CrossOrigin
public class InstagramOAuthController {

    @Autowired
    private InstagramOAuthService instagramOAuthService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @GetMapping("/auth/instagram/url")
    public ResponseEntity<?> instagramAuthUrl() {
        String url = instagramOAuthService.buildAuthorizeUrl();
        boolean dev = !instagramOAuthService.isConfigured();
        return ResponseEntity.ok(dev
                ? Map.of("url", url, "devMode", true)
                : Map.of("url", url));
    }

    @GetMapping("/auth/instagram/dev-callback")
    public void instagramDevCallback(HttpServletResponse response) throws IOException {
        response.sendRedirect(instagramOAuthService.finishDevOAuth());
    }

    @GetMapping("/auth/instagram/callback")
    public void instagramCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpServletResponse response) throws IOException {
        String base = frontendUrl.replaceAll("/$", "") + "/oauth/instagram";
        if (error != null && !error.isBlank()) {
            String msg = (error_description != null && !error_description.isBlank()) ? error_description : error;
            response.sendRedirect(base + "?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
            return;
        }
        response.sendRedirect(instagramOAuthService.finishOAuth(code, state));
    }
}
