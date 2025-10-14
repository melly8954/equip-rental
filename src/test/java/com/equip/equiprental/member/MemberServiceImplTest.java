package com.equip.equiprental.member;

import com.equip.equiprental.common.exception.CustomException;
import com.equip.equiprental.common.exception.ErrorType;
import com.equip.equiprental.common.dto.PageResponseDto;
import com.equip.equiprental.member.domain.Department;
import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.dto.*;
import com.equip.equiprental.member.repository.DepartmentRepository;
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
    @Mock DepartmentRepository departmentRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    MemberServiceImpl memberService;

    @Nested
    @DisplayName("signUp 메서드 테스트")
    class SignUp {
        private SignUpRequest dto;
        private Department mockDepartment;

        @BeforeEach
        void setUp() {
            mockDepartment = new Department(1L, "testdepartment");

            dto = SignUpRequest.builder()
                    .username("testid")
                    .password("testpassword")
                    .confirmPassword("testpassword")
                    .name("testname")
                    .departmentId(1L)
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
            when(departmentRepository.findById(dto.getDepartmentId())).thenReturn(Optional.of(mockDepartment));

            // when
            SignUpResponse response = memberService.signUp(dto);

            // then
            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository, times(1)).save(captor.capture());
            Member savedMember = captor.getValue();

            assertThat(savedMember.getUsername()).isEqualTo(dto.getUsername());
            assertThat(savedMember.getPassword()).isEqualTo("encodedPassword");
            assertThat(savedMember.getEmail()).isEqualTo(dto.getEmail());
            assertThat(savedMember.getName()).isEqualTo(dto.getName());
            assertThat(savedMember.getRole()).isEqualTo(MemberRole.USER);
            assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.PENDING);
            assertThat(savedMember.getDepartment()).isEqualTo(mockDepartment);

            // Response DTO 검증
            assertThat(response.getMemberId()).isEqualTo(savedMember.getMemberId());
            assertThat(response.getUsername()).isEqualTo(dto.getUsername());
            assertThat(response.getName()).isEqualTo(dto.getName());
            assertThat(response.getDepartment()).isEqualTo(mockDepartment.getDepartmentName());
            assertThat(response.getEmail()).isEqualTo(dto.getEmail());
            assertThat(response.getCreatedAt()).isEqualTo(savedMember.getCreatedAt());
        }

        @Test
        @DisplayName("예외 - 중복된 username 사용")
        void signUp_duplicateUsername_throwsException() {
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(true);

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.CONFLICT);
        }

        @Test
        @DisplayName("예외 - 중복된 email 사용")
        void signUp_duplicateEmail_throwsException() {
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(memberRepository.existsByEmail(dto.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.CONFLICT);
        }

        @Test
        @DisplayName("예외 - 비밀번호 불일치")
        void signUp_passwordMismatch_throwsException() {
            dto.setConfirmPassword("passwordmismatch");
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(memberRepository.existsByEmail(dto.getEmail())).thenReturn(false);

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 부서")
        void signUp_departmentNotFound_throwsException() {
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(memberRepository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(departmentRepository.findById(dto.getDepartmentId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.signUp(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("searchMembers 메서드 테스트")
    class searchMembers {
        Member member;
        MemberFilter dto;
        Department dept;

        @BeforeEach
        void setUp() {
            dept = new Department(1L, "TestDept");

            member = Member.builder()
                    .department(dept)
                    .status(MemberStatus.ACTIVE)
                    .role(MemberRole.ADMIN)
                    .build();
        }

        @SuppressWarnings("unchecked")
        private Page<Member> mockPageWithContent(List<Member> members) {
            Page<Member> page = mock(Page.class);
            when(page.getContent()).thenReturn(members);
            when(page.getNumber()).thenReturn(0);
            when(page.getSize()).thenReturn(10);
            when(page.getTotalElements()).thenReturn((long) members.size());
            when(page.getTotalPages()).thenReturn(1);
            when(page.isFirst()).thenReturn(true);
            when(page.isLast()).thenReturn(true);
            when(page.isEmpty()).thenReturn(members.isEmpty());
            when(page.getNumberOfElements()).thenReturn(members.size());
            return page;
        }

        @Test
        @DisplayName("성공 - status와 role 모두 존재할 때 findByStatusAndRole 호출")
        void whenStatusAndRoleNotNull_thenFindByStatusAndRoleCalled() {
            dto = MemberFilter.builder()
                    .page(1)
                    .size(10)
                    .status(MemberStatus.ACTIVE)
                    .role(MemberRole.ADMIN)
                    .build();

            Page<Member> page = mockPageWithContent(List.of(member));
            when(memberRepository.findByStatusAndRole(MemberStatus.ACTIVE, MemberRole.ADMIN, dto.getPageable()))
                    .thenReturn(page);

            PageResponseDto<MemberDto> response = memberService.searchMembers(dto);

            verify(memberRepository).findByStatusAndRole(MemberStatus.ACTIVE, MemberRole.ADMIN, dto.getPageable());
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - status만 존재할 때 findByStatus 호출")
        void whenStatusNotNullAndRoleNull_thenFindByStatusCalled() {
            dto = MemberFilter.builder()
                    .page(1)
                    .size(10)
                    .status(MemberStatus.ACTIVE)
                    .build();

            Page<Member> page = mockPageWithContent(List.of(member));
            when(memberRepository.findByStatus(MemberStatus.ACTIVE, dto.getPageable()))
                    .thenReturn(page);

            memberService.searchMembers(dto);

            verify(memberRepository).findByStatus(MemberStatus.ACTIVE, dto.getPageable());
        }

        @Test
        @DisplayName("성공 - role만 존재할 때 findByRole 호출")
        void whenStatusNullAndRoleNotNull_thenFindByRoleCalled() {
            dto = MemberFilter.builder()
                    .page(1)
                    .size(10)
                    .role(MemberRole.ADMIN)
                    .build();

            Page<Member> page = mockPageWithContent(List.of(member));
            when(memberRepository.findByRole(MemberRole.ADMIN, dto.getPageable()))
                    .thenReturn(page);

            memberService.searchMembers(dto);

            verify(memberRepository).findByRole(MemberRole.ADMIN, dto.getPageable());
        }

        @Test
        @DisplayName("성공 - status와 role 모두 null일 때 findAll 호출")
        void whenStatusAndRoleNull_thenFindAllCalled() {
            dto = MemberFilter.builder()
                    .page(1)
                    .size(10)
                    .build();

            Page<Member> page = mockPageWithContent(List.of(member));
            when(memberRepository.findAll(dto.getPageable())).thenReturn(page);

            memberService.searchMembers(dto);

            verify(memberRepository).findAll(dto.getPageable());
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
            UpdateMemberRequest dto = new UpdateMemberRequest(MemberStatus.ACTIVE,null);

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
            UpdateMemberRequest dto = new UpdateMemberRequest(MemberStatus.ACTIVE,null);

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateMemberStatus(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
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
            UpdateMemberRequest dto = new UpdateMemberRequest(null,MemberRole.MANAGER);

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
            UpdateMemberRequest dto = new UpdateMemberRequest(null,MemberRole.MANAGER);

            when(memberRepository.findByMemberId(1L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateMemberStatus(1L, dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}

