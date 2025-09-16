package com.equip.equiprental.member.repository;

import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.domain.MemberRole;
import com.equip.equiprental.member.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Member> findByMemberId(Long memberId);
    Optional<Member> findByUsername(String username);

    // 사용자 관리 API 조회
    Page<Member> findByStatusAndRole(MemberStatus status, MemberRole role, Pageable pageable);
    Page<Member> findByStatus(MemberStatus status, Pageable pageable);
    Page<Member> findByRole(MemberRole role, Pageable pageable);
}
