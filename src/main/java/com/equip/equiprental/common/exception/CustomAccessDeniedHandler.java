package com.equip.equiprental.common.exception;

import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.interceptor.RequestTraceIdInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String traceId = RequestTraceIdInterceptor.getTraceId();

        ResponseDto<?> dto = new ResponseDto<>(
                traceId,
                HttpServletResponse.SC_FORBIDDEN,
                ErrorType.FORBIDDEN.getErrorCode(),
                ErrorType.FORBIDDEN.getMessage(),
                null);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
