package com.scripto.backend.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimitService loginRateLimitService;

    @Value("${scripto.security.trust-forwarded-for:false}")
    private boolean trustForwardedFor;

    public LoginRateLimitFilter(LoginRateLimitService loginRateLimitService) {
        this.loginRateLimitService = loginRateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isCredentialVerificationRequest(request)) {
            String ip = resolveClientIp(request);
            Bucket bucket = loginRateLimitService.resolveBucket(ip);

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"status\":429,\"error\":\"Muitas Tentativas\",\"message\":\"Muitas tentativas de autenticação. Tente novamente em um minuto.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustForwardedFor) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String candidate = forwardedFor.split(",", 2)[0].trim();
                if (!candidate.isBlank() && candidate.length() <= 64) {
                    return candidate;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isCredentialVerificationRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String normalized = uri == null ? "" : uri.replaceFirst("/+$", "");
        if ("/user/login".equals(normalized) || "/user/reactivate".equals(normalized)) {
            return "POST".equalsIgnoreCase(request.getMethod());
        }
        return "/user/suspended/password".equals(normalized)
                && "PATCH".equalsIgnoreCase(request.getMethod());
    }
}
