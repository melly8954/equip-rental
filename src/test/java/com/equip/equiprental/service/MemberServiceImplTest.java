package com.equip.equiprental.service;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.response.PageResponseDto;
import com.equip.equiprental.common.response.SearchParamDto;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.dto.*;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl 단위 테스트")
public class MemberServiceImplTest {
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock Page<Member> mockPage;
    @Mock Page<Member> emptyPage;

    @InjectMocks
    MemberServiceImpl memberService;

    @Nested
    @DisplayName("signUp 메서드 테스트")
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

    @Nested
    @DisplayName("searchMembers 메서드 테스트")
    class searchMembers {
        Member member;
        SearchParamDto dto;

        @BeforeEach
        void setUp() {
            dto = SearchParamDto.builder()
                    .page(1)
                    .size(10)
                    .build();

            member = Member.builder()
                    .status(MemberStatus.ACTIVE)
                    .role(MemberRole.ADMIN)
                    .build();
        }

        @Test
        @DisplayName("성공 - status와 role 모두 존재할 때 findByStatusAndRole 호출")
        void whenStatusAndRoleNotNull_thenFindByStatusAndRoleCalled() {
            // given
            dto.setStatus("ACTIVE");
            dto.setRole("ADMIN");

            when(mockPage.getContent()).thenReturn(List.of(member));
            when(memberRepository.findByStatusAndRole(MemberStatus.ACTIVE, MemberRole.ADMIN, dto.getPageable()))
                    .thenReturn(mockPage);

            // when
            memberService.searchMembers(dto);

            // then
            verify(memberRepository).findByStatusAndRole(MemberStatus.ACTIVE, MemberRole.ADMIN, dto.getPageable());
        }

        @Test
        @DisplayName("성공 - status만 존재할 때 findByStatus 호출")
        void whenStatusNotNullAndRoleNull_thenFindByStatusCalled() {
            // given
            dto.setStatus("ACTIVE");

            when(mockPage.getContent()).thenReturn(List.of(member));
            when(memberRepository.findByStatus(MemberStatus.ACTIVE, dto.getPageable()))
                    .thenReturn(mockPage);

            // when
            memberService.searchMembers(dto);

            // then
            verify(memberRepository).findByStatus(MemberStatus.ACTIVE, dto.getPageable());
        }

        @Test
        @DisplayName("성공 - role만 존재할 때 findByRole 호출")
        void whenStatusNullAndRoleNotNull_thenFindByRoleCalled() {
            // given
            dto.setRole("ADMIN");

            when(mockPage.getContent()).thenReturn(List.of(member));
            when(memberRepository.findByRole(MemberRole.ADMIN, dto.getPageable()))
                    .thenReturn(mockPage);

            // when
            memberService.searchMembers(dto);

            // then
            verify(memberRepository).findByRole(MemberRole.ADMIN, dto.getPageable());
        }

        @Test
        @DisplayName("성공 - status와 role 모두 null일 때 findAll 호출")
        void whenStatusAndRoleNull_thenFindAllCalled() {
            // given
            when(mockPage.getContent()).thenReturn(List.of(member));
            when(memberRepository.findAll(dto.getPageable()))
                    .thenReturn(mockPage);

            // when
            memberService.searchMembers(dto);

            // then
            verify(memberRepository).findAll(dto.getPageable());
        }

        @Test
        @DisplayName("성공 - 검색 결과가 빈 페이지일 때 empty=true")
        void whenEmptyPageReturned_thenEmptyFlagTrue() {
            // given
            when(emptyPage.getContent()).thenReturn(List.of());
            when(emptyPage.isEmpty()).thenReturn(true);
            when(emptyPage.getTotalElements()).thenReturn(0L);
            when(emptyPage.isFirst()).thenReturn(true);
            when(emptyPage.isLast()).thenReturn(true);
            when(memberRepository.findAll(dto.getPageable())).thenReturn(emptyPage);

            // when
            PageResponseDto<MemberDto> response = memberService.searchMembers(dto);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.isEmpty()).isTrue();
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.getTotalElements()).isEqualTo(0L);
        }

        @Test
        @DisplayName("검색 결과가 존재할 때 페이징 정보 정상 반환")
        void whenPageHasContent_thenPagingInfoCorrect() {
            // given
            dto.setStatus("ACTIVE");
            dto.setRole("ADMIN");

            when(mockPage.getContent()).thenReturn(List.of(member));
            when(mockPage.getNumber()).thenReturn(0);
            when(mockPage.getSize()).thenReturn(10);
            when(mockPage.getTotalElements()).thenReturn(1L);
            when(mockPage.getTotalPages()).thenReturn(1);
            when(mockPage.isFirst()).thenReturn(true);
            when(mockPage.isLast()).thenReturn(true);
            when(mockPage.isEmpty()).thenReturn(false);
            when(memberRepository.findByStatusAndRole(MemberStatus.ACTIVE, MemberRole.ADMIN, dto.getPageable()))
                    .thenReturn(mockPage);

            // when
            PageResponseDto<MemberDto> response = memberService.searchMembers(dto);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.isEmpty()).isFalse();
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
            assertThat(response.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("예외 - 잘못된 status 입력 시 CustomException 발생")
        void whenInvalidStatus_thenThrowCustomException() {
            // given
            dto.setStatus("INVALID");

            // when & then
            assertThatThrownBy(() -> memberService.searchMembers(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_STATUS_REQUEST);
        }

        @Test
        @DisplayName("예외 - 잘못된 role 입력 시 CustomException 발생")
        void whenInvalidRole_thenThrowCustomException() {
            // given
            dto.setRole("INVALID");

            // when & then
            assertThatThrownBy(() -> memberService.searchMembers(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_ROLE_REQUEST);
        }
    }

    @Nested
    @DisplayName("updateMemberStatus 메서드 테스트")
    class updateMemberStatus {
        Member member;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .memberId(1L)
                    .status(MemberStatus.PENDING)
                    .build();
        }

        @Test
        @DisplayName("성공 - 사용자 상태 변경")
        void updateStatus_success() {
            // given
            UpdateMemberRequest dto = new UpdateMemberRequest("ACTIVE",null);

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));

            // when
            UpdateMemberStatusResponse result = memberService.updateMemberStatus(1L, dto);

            // then
            assertThat(result.getMemberId()).isEqualTo(1L);
            assertThat(result.getOldStatus()).isEqualTo("PENDING");
            assertThat(result.getNewStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 사용자")
        void updateStatus_userNotFound() {
            // given
            UpdateMemberRequest dto = new UpdateMemberRequest("ACTIVE",null);

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateMemberStatus(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 잘못된 상태 변경 요청")
        void updateStatus_invalid_request() {
            // given
            UpdateMemberRequest dto = new UpdateMemberRequest("INVALID",null);

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> memberService.updateMemberStatus(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_STATUS_REQUEST);
        }
    }

    @Nested
    @DisplayName("updateMemberRole 메서드 테스트")
    class updateMemberRole {
        Member member;

        @BeforeEach
        void setUp() {
            member = Member.builder()
                    .memberId(1L)
                    .role(MemberRole.USER)
                    .build();
        }

        @Test
        @DisplayName("성공 - 사용자 역할 변경")
        void updateRole_success() {
            // given
            UpdateMemberRequest dto = new UpdateMemberRequest(null,"MANAGER");

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));

            // when
            UpdateMemberRoleResponse result = memberService.updateMemberRole(1L, dto);

            // then
            assertThat(result.getMemberId()).isEqualTo(1L);
            assertThat(result.getOldRole()).isEqualTo("USER");
            assertThat(result.getNewRole()).isEqualTo("MANAGER");
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 사용자")
        void updateRole_userNotFound() {
            // given
            UpdateMemberRequest dto = new UpdateMemberRequest(null,"MANAGER");

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateMemberStatus(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 잘못된 역할 변경 요청")
        void updateRole_invalid_request() {
            // given
            UpdateMemberRequest dto = new UpdateMemberRequest(null,"INVALID");

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> memberService.updateMemberRole(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_ROLE_REQUEST);
        }
    }
}

