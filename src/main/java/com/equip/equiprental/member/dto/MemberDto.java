package com.equip.equiprental.member.dto;

import com.equip.equiprental.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    public MemberDto(Member member) {
        this.memberId = member.getMemberId();
        this.username = member.getUsername();
        this.name = member.getName();
        this.department = member.getDepartment();
        this.email = member.getEmail();
        this.status = member.getStatus().name(); // Enum이면 name()으로 String 변환
        this.role = member.getRole().name();
        this.createdAt = member.getCreatedAt();
    }
}
