package com.equip.equiprental.common.exception;

import com.equip.equiprental.common.dto.ResponseDto;
import com.equip.equiprental.common.filter.RequestTraceIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String traceId = RequestTraceIdFilter.getTraceId();

        ResponseDto<?> dto = new ResponseDto<>(
                traceId,
                HttpServletResponse.SC_UNAUTHORIZED,
                ErrorType.UNAUTHORIZED.getErrorCode(),
                ErrorType.UNAUTHORIZED.getMessage(),
                null);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
