package com.equip.equiprental.common.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AllViewController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signup")
    public String signup() {
        return "member/signup";
    }

    @GetMapping("/home")
    public String home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getAuthorities() != null) {
            boolean isAdminOrManager  = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER"));

            if (isAdminOrManager ) {
                return "adminHome";  // 관리자용 페이지
            }
        }

        return "userHome";  // 일반 사용자 페이지
    }

    @GetMapping("/member")
    @PreAuthorize("hasRole('ADMIN')")
    public String member() {
        return "member/memberList";
    }

    @GetMapping("/error/forbidden")
    public String errorForbidden() {
        return "error/forbidden";
    }
}
