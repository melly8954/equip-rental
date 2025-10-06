package com.equip.equiprental.member.dto;

import com.equip.equiprental.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDto {
    private Long memberId;
    private String username;
    private String name;
    private String department;
    private String email;
    private String status;
    private String role;
    private List<String> categories;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public MemberDto(Member member, List<String> categories) {
        this.memberId = member.getMemberId();
        this.username = member.getUsername();
        this.name = member.getName();
        this.department = member.getDepartment().getDepartmentName();
        this.email = member.getEmail();
        this.status = member.getStatus().name(); // Enum이면 name()으로 String 변환
        this.role = member.getRole().name();
        this.categories = categories;
        this.createdAt = member.getCreatedAt();
    }
}
