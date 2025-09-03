package com.equip.equiprental.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTraceIdFilter extends OncePerRequestFilter {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void clear() {
        TRACE_ID.remove();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            TRACE_ID.set(UUID.randomUUID().toString());
            filterChain.doFilter(request, response);
        } finally {
            TRACE_ID.remove();
        }
    }
}