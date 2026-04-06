package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InstagramOAuthService {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final Map<String, Long> pendingStates = new ConcurrentHashMap<>();

    @Value("${instagram.client-id:}")
    private String clientId;

    @Value("${instagram.client-secret:}")
    private String clientSecret;

    @Value("${instagram.redirect-uri:http://localhost:8080/auth/instagram/callback}")
    private String redirectUri;

    @Value("${instagram.scope:user_profile,user_media}")
    private String scope;

    @Value("${instagram.authorize-url:https://api.instagram.com/oauth/authorize}")
    private String authorizeUrl;

    @Value("${instagram.token-url:https://api.instagram.com/oauth/access_token}")
    private String tokenUrl;

    @Value("${instagram.graph-me-url:https://graph.instagram.com/me}")
    private String graphMeUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /** Public URL of this API (browser must open this for OAuth / dev callback). */
    @Value("${app.public-backend-url:http://localhost:8080}")
    private String publicBackendUrl;

    @Value("${instagram.dev-instagram-id:dev_ig_user_001}")
    private String devInstagramId;

    @Value("${instagram.dev-username:dev_influencer}")
    private String devInstagramUsername;

    @Autowired
    private AuthService authService;

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    public String getRedirectUriForHint() {
        return redirectUri;
    }

    /**
     * Browser redirect URL: real Instagram when client-id/secret are set; otherwise local dev mock
     * (no Meta app). Never empty — avoids 503 when credentials are unset.
     */
    public String buildAuthorizeUrl() {
        pruneStates();
        String state = UUID.randomUUID().toString();
        pendingStates.put(state, System.currentTimeMillis());

        String q = "client_id=" + enc(clientId)
                + "&redirect_uri=" + enc(redirectUri)
                + "&scope=" + enc(scope)
                + "&response_type=code"
                + "&state=" + enc(state);
        return authorizeUrl + "?" + q;
    }

    /**
     * Dev callback is not allowed when using real Instagram OAuth.
     */
    public String finishDevOAuth() {
        String base = frontendUrl.replaceAll("/$", "") + "/oauth/instagram";
        return base + "?error=" + encQ("dev_callback_not_allowed");
    }

    public String finishOAuth(String code, String state) {
        String base = frontendUrl.replaceAll("/$", "") + "/oauth/instagram";
        try {
            if (code == null || code.isBlank() || state == null || state.isBlank()) {
                return base + "?error=" + encQ("missing_code_or_state");
            }
            pruneStates();
            Long created = pendingStates.remove(state);
            if (created == null || System.currentTimeMillis() - created > STATE_TTL.toMillis()) {
                return base + "?error=" + encQ("invalid_state");
            }

            String form = "client_id=" + enc(clientId)
                    + "&client_secret=" + enc(clientSecret)
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + enc(redirectUri)
                    + "&code=" + enc(code);

            HttpRequest tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> tokenRes = httpClient.send(tokenReq, HttpResponse.BodyHandlers.ofString());
            if (tokenRes.statusCode() / 100 != 2) {
                return base + "?error=" + encQ("token_exchange_failed");
            }

            JsonNode tokenJson = objectMapper.readTree(tokenRes.body());
            if (tokenJson.hasNonNull("error_message")) {
                return base + "?error=" + encQ(tokenJson.get("error_message").asText());
            }
            if (!tokenJson.hasNonNull("access_token")) {
                return base + "?error=" + encQ("no_access_token");
            }
            String accessToken = tokenJson.get("access_token").asText();

            String meUrl = graphMeUrl + "?fields=id,username&access_token=" + enc(accessToken);
            HttpRequest meReq = HttpRequest.newBuilder()
                    .uri(URI.create(meUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> meRes = httpClient.send(meReq, HttpResponse.BodyHandlers.ofString());
            if (meRes.statusCode() / 100 != 2) {
                return base + "?error=" + encQ("profile_fetch_failed");
            }

            JsonNode me = objectMapper.readTree(meRes.body());
            if (me.hasNonNull("error")) {
                return base + "?error=" + encQ(me.get("error").asText());
            }
            String igId = me.hasNonNull("id") ? me.get("id").asText() : null;
            String igUser = me.hasNonNull("username") ? me.get("username").asText() : null;

            String jwt = authService.provisionOrLoginInfluencerFromInstagram(igId, igUser);
            if (jwt == null) {
                return base + "?error=" + encQ("username_conflict_or_invalid_account");
            }
            return base + "?token=" + encQ(jwt);
        } catch (Exception e) {
            return base + "?error=" + encQ("oauth_error");
        }
    }

    private void pruneStates() {
        long cutoff = System.currentTimeMillis() - STATE_TTL.toMillis();
        pendingStates.entrySet().removeIf(e -> e.getValue() < cutoff);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String encQ(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
