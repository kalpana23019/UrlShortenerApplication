package com.app.controller;

import com.app.entity.ShortUrl;
import com.app.service.AnalyticsService;
import com.app.service.ClickService;
import com.app.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class UrlController {
    private final UrlService urls;
    private final ClickService clicks;
    private final AnalyticsService analytics;

    public UrlController(UrlService urls, ClickService clicks, AnalyticsService analytics) {
        this.urls = urls; this.clicks = clicks; this.analytics = analytics;
    }

    // Request body for creating a short URL
    public record CreateRequest(String url, String alias, LocalDateTime expiresAt) {}

    // CREATE SHORT URL
    @PostMapping("/api/urls")
    public Map<String, Object> create(@RequestBody CreateRequest req, HttpServletRequest http) {
        ShortUrl s = urls.create(req.url(), req.alias(), req.expiresAt());
        String base = http.getScheme() + "://" + http.getServerName() + ":" + http.getServerPort();
        return Map.of("code", s.getCode(), "shortUrl", base + "/" + s.getCode());
    }

    // ANALYTICS
    @GetMapping("/api/urls/{code}/analytics")
    public Map<String, Object> stats(@PathVariable String code) {
        if (!urls.exists(code)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return analytics.get(code);
    }

    // DEACTIVATE
    @PatchMapping("/api/urls/{code}/deactivate")
    public Map<String, String> deactivate(@PathVariable String code) {
        urls.deactivate(code);
        return Map.of("message", "Link deactivated");
    }
    // REDIRECT
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest req) {
        // 1. Find URL from cache/DB
        ShortUrl s = urls.resolve(code);

        // 2. Get visitor IP
        String xff = req.getHeader("X-Forwarded-For");
        String ip ;
        if (xff != null && !xff.isBlank())
        {
            ip= xff.split(",")[0].trim();
        } else{
            ip = req.getRemoteAddr();
        }
        // 3. Get browser information
        String userAgent = req.getHeader("User-Agent");

        // 4. Get referrer
        String referrer = req.getHeader("Referer");

        // 5. Record analytics asynchronously
        clicks.record(
                code,
                ip,
                userAgent,
                referrer
        );

        // 6. Redirect user immediately
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(s.getLongUrl())).build();
    }
}