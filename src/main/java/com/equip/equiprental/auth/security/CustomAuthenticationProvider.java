package com.equip.equiprental.auth.security;

import com.equip.equiprental.member.domain.Member;
import com.equip.equiprental.member.repository.MemberRepository;
import com.equip.equiprental.member.domain.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("BAD_CREDENTIALS"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BadCredentialsException("BAD_CREDENTIALS");
        }

        // 계정 상태 체크
        if (member.getStatus() == MemberStatus.PENDING) {
            throw new DisabledException("USER_PENDING");
        }

        if (member.getStatus() == MemberStatus.DELETED) {
            throw new DisabledException("USER_DELETED");
        }

        // 인증 토큰과 권한 생성
        PrincipalDetails principalDetails = new PrincipalDetails(member);

        return new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
