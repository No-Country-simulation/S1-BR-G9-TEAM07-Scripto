package com.scripto.backend.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimitService loginRateLimitService;

    public LoginRateLimitFilter(LoginRateLimitService loginRateLimitService) {
        this.loginRateLimitService = loginRateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("/user/login/".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = request.getRemoteAddr();

            System.out.println("Requisição recebida do IP: " + ip);

            Bucket bucket = loginRateLimitService.resolveBucket(ip);

            if (!bucket.tryConsume(1)) {
                System.out.println("Deu a cota!");
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Muitas tentativas. Tente novamente em um minuto.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}