package com.equip.equiprental.common.route;

import com.equip.equiprental.auth.security.PrincipalDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    @ModelAttribute
    public void addMemberToModel(Model model, @AuthenticationPrincipal PrincipalDetails principal) {
        if (principal != null) {
            model.addAttribute("member", principal.getMember());
        }
    }
}
