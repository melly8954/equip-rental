package com.equip.equiprental.auth;

import com.equip.equiprental.auth.service.AuthServiceImpl;
import com.equip.equiprental.auth.security.PrincipalDetails;
import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.Department;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import com.equip.equiprental.auth.dto.LoginRequestDto;
import com.equip.equiprental.auth.dto.LoginResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.core.context.SecurityContextHolder;

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
    @Mock private HttpServletResponse httpResponse;
    @Mock private HttpSession httpSession;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("login() 메서드 테스트")
    class login {
        private Department mockDepartment;

        @BeforeEach
        void setUp() {
            mockDepartment = new Department(1L, "testdepartment");
        }

        @Test
        @DisplayName("성공 - 로그인 성공")
        void login_success() {
            // given

            LoginRequestDto dto = new LoginRequestDto("testUser", "password");
            Member member = Member.builder()
                    .memberId(1L)
                    .username("testUser")
                    .name("테스트")
                    .department(mockDepartment)
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
            assertThat(response.getDepartment()).isEqualTo("testdepartment");
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

    @Nested
    @DisplayName("logout() 메서드 테스트")
    class logout {
        @Test
        @DisplayName("성공 - 로그아웃 시 세션 무효화, SecurityContext 초기화, JSESSIONID 쿠키 삭제")
        void logout_success() {
            // given
            when(httpRequest.getSession(false)).thenReturn(httpSession);
            Cookie jsessionCookie = new Cookie("JSESSIONID", "12345");
            when(httpRequest.getCookies()).thenReturn(new Cookie[]{jsessionCookie});

            // SecurityContext에 임의 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(mock(org.springframework.security.core.Authentication.class));

            // when
            authService.logout(httpRequest, httpResponse);

            // then
            // 세션 무효화 확인
            verify(httpSession).invalidate();

            // SecurityContext 초기화 확인
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

            // 쿠키 삭제 확인
            verify(httpResponse).addCookie(argThat(cookie ->
                    "JSESSIONID".equals(cookie.getName()) &&
                            cookie.getMaxAge() == 0 &&
                            cookie.getValue() == null
            ));
        }

        @Test
        @DisplayName("세션이 없는 경우 - logout() 호출 시 아무 동작 없음")
        void logout_noSession() {
            when(httpRequest.getSession(false)).thenReturn(null);
            when(httpRequest.getCookies()).thenReturn(null);

            authService.logout(httpRequest, httpResponse);

            // 세션 invalidate 호출 안 됨
            verify(httpSession, never()).invalidate();

            // 쿠키 삭제도 없음
            verify(httpResponse, never()).addCookie(any());
        }
    }
}
