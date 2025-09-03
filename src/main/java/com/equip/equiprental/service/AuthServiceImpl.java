package com.equip.equiprental.service;

import com.equip.equiprental.common.auth.PrincipalDetails;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.domain.member.Member;
import com.equip.equiprental.dto.auth.LoginRequestDto;
import com.equip.equiprental.dto.auth.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponseDto login(HttpServletRequest httpRequest, LoginRequestDto dto) {
        try{
            // 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());

            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(authToken);

            // SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션 생성 및 SecurityContext 저장
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
            Member member = principal.getMember();

            return LoginResponseDto.builder()
                    .memberId(member.getMemberId())
                    .username(member.getUsername())
                    .name(member.getName())
                    .department(member.getDepartment())
                    .email(member.getEmail())
                    .role(member.getRole().name())
                    .status(member.getStatus().name())
                    .build();
        } catch (BadCredentialsException e) {
            throw new CustomException(ErrorType.BAD_CREDENTIALS);
        } catch (DisabledException e) {
            if ("USER_PENDING".equals(e.getMessage())) {
                throw new CustomException(ErrorType.USER_PENDING);
            } else if("USER_DELETED".equals(e.getMessage())){
                throw new CustomException(ErrorType.USER_DELETED);
            } else{
                throw e;
            }
        } catch (AuthenticationException e) {
            throw new CustomException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}
