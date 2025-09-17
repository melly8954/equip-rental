package com.equip.equiprental.common.route;

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

        return "equipment/equipmentList";  // 일반 사용자 페이지
    }

    // 사용자 접근
    @GetMapping("/equipment/list")
    public String equipmentList() {
        return "equipment/equipmentList";
    }

    @GetMapping("/rental/list")
    public String userRentalList() {
        return "rental/rentalList";
    }

    @GetMapping("/rental/item/list")
    public String userRentalItemList() {
        return "rental/rentalItemList";
    }

    // 관리자 접근
    @GetMapping("/member")
    @PreAuthorize("hasRole('ADMIN')")
    public String member() {
        return "member/memberList";
    }

    @GetMapping("/admin/equipment/registration")
    @PreAuthorize("hasRole('ADMIN')")
    public String equipmentRegistration() {
        return "equipment/admin/equipmentRegistration";
    }

    @GetMapping("/admin/equipment/list")
    public String equipmentListInAdmin() {
        return "equipment/admin/adminEquipmentList";
    }

    @GetMapping("/admin/equipment/{equipmentId}/item")
    public String equipmentItemList() {
        return "equipment/admin/equipmentItemList";
    }

    @GetMapping("/admin/equipment/{equipmentId}/item/{itemId}/history")
    public String equipmentItemHistory() {
        return "equipment/admin/equipmentItemHistory";
    }

    @GetMapping("/admin/rental/list")
    public String adminRentalList() {
        return "rental/admin/adminRentalList";
    }

    @GetMapping("/admin/rental/item/list")
    public String adminRentalItemList() {
        return "rental/admin/adminRentalItemList";
    }

    // 에러 페이지
    @GetMapping("/error/unauthorized")
    public String errorUnAuthorized() {
        return "error/unauthorized";
    }

    @GetMapping("/error/forbidden")
    public String errorForbidden() {
        return "error/forbidden";
    }
}
