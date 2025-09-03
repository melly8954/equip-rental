package com.equip.equiprental.service;

import com.equip.equiprental.common.auth.PrincipalDetails;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.domain.member.Member;
import com.equip.equiprental.domain.member.MemberRole;
import com.equip.equiprental.domain.member.MemberStatus;
import com.equip.equiprental.dto.auth.LoginRequestDto;
import com.equip.equiprental.dto.auth.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 단위 테스트")
public class AuthServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock private HttpServletRequest httpRequest;
    @Mock private HttpSession httpSession;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("login() 메서드 테스트")
    class login {
        @Test
        @DisplayName("성공 - 로그인 성공")
        void login_success() {
            // given
            LoginRequestDto dto = new LoginRequestDto("testUser", "password");
            Member member = Member.builder()
                    .memberId(1L)
                    .username("testUser")
                    .name("테스트")
                    .department("개발")
                    .email("test@example.com")
                    .role(MemberRole.USER)
                    .status(MemberStatus.ACTIVE) // 여기 주의: ACTIVE로 세팅
                    .build();
            PrincipalDetails principal = new PrincipalDetails(member);

            when(httpRequest.getSession(true)).thenReturn(httpSession);
            when(authentication.getPrincipal()).thenReturn(principal);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            // when
            LoginResponseDto response = authService.login(httpRequest, dto);

            // then
            assertThat(response.getMemberId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("testUser");
            assertThat(response.getName()).isEqualTo("테스트");
            assertThat(response.getDepartment()).isEqualTo("개발");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getRole()).isEqualTo("USER");
            assertThat(response.getStatus()).isEqualTo("ACTIVE");

            verify(httpSession, times(1))
                    .setAttribute(any(), any());
        }

        @Test
        @DisplayName("예외 - 로그인 비밀번호 불일치")
        void login_badCredentials() {
            LoginRequestDto dto = new LoginRequestDto("testUser", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(httpRequest, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_CREDENTIALS);
        }

        @Test
        @DisplayName("예외 - 사용자 비활성화 (USER_DELETED)")
        void login_disabledUserDeleted() {
            LoginRequestDto dto = new LoginRequestDto("testUser", "password");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new DisabledException("USER_DELETED"));

            assertThatThrownBy(() -> authService.login(httpRequest, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_DELETED);
        }

        @Test
        @DisplayName("예외 - 사용자 미승인 (USER_PENDING)")
        void login_disabledUserInactive() {
            LoginRequestDto dto = new LoginRequestDto("testUser", "password");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new DisabledException("USER_PENDING"));

            assertThatThrownBy(() -> authService.login(httpRequest, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_PENDING);
        }
    }
}
