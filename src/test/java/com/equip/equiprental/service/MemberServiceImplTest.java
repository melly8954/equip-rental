package com.equip.equiprental.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import com.equip.equiprental.member.dto.SignUpRequestDto;
import com.equip.equiprental.member.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl 단위 테스트")
public class MemberServiceImplTest {
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    MemberServiceImpl memberService;

    @Nested
    @DisplayName("signUp() 메서드 테스트")
    class SignUp {
        private SignUpRequestDto dto;

        @BeforeEach
        void setUp() {
            dto = SignUpRequestDto.builder()
                    .username("testid")
                    .password("testpassword")
                    .confirmPassword("testpassword")
                    .name("testname")
                    .department("testdepartment")
                    .email("testid@example.com")
                    .build();
        }

        @Test
        @DisplayName("성공 - 회원가입 성공")
        void signUp_success() {
            // given
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(memberRepository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

            // when
            memberService.signUp(dto);

            // then
            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository, times(1)).save(captor.capture());
            Member savedUser = captor.getValue();

            assertThat(savedUser.getUsername()).isEqualTo(dto.getUsername());
            assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
            assertThat(savedUser.getEmail()).isEqualTo(dto.getEmail());
            assertThat(savedUser.getRole()).isEqualTo(MemberRole.USER);
            assertThat(savedUser.getStatus()).isEqualTo(MemberStatus.PENDING);
        }

        @Test
        @DisplayName("예외 - 중복된 username 사용")
        void signUp_duplicateUsername_throwsException() {
            when(memberRepository.existsByUsername("testid")).thenReturn(true);

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.DUPLICATE_USERNAME);
        }

        @Test
        @DisplayName("예외 - 중복된 email 사용")
        void signUp_duplicateEmail_throwsException() {
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(memberRepository.existsByEmail("testid@example.com")).thenReturn(true);

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.DUPLICATE_EMAIL);
        }

        @Test
        @DisplayName("예외 - 비밀번호 불일치")
        void signUp_passwordMismatch_throwsException() {
            dto.setConfirmPassword("passwordmismatch");
            when(memberRepository.existsByUsername("testid")).thenReturn(false);
            when(memberRepository.existsByEmail("testid@example.com")).thenReturn(false);

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.PASSWORD_MISMATCH);
        }
    }
}

