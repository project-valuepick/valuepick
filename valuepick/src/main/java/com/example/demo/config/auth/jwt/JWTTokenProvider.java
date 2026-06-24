package com.example.demo.config.auth.jwt;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.UserRole;
import com.example.demo.domain.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JWTTokenProvider {

    @Autowired
    private UserRepository userRepository;

    private Key key;

    // 앱 시작 시 한 번 실행 - 서명에 사용할 키 생성
    @PostConstruct
    public void init() {
        byte[] keyBytes = KeyGenerator.keyGen();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWTTokenProvider init - key generated");
    }

    // 로그인 성공 시 AccessToken + RefreshToken 생성
    public TokenInfo generateToken(Authentication authentication) {

        // authentication 에서 role 꺼내기 -> "ROLE_USER"
        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = new Date().getTime();

        // AccessToken: username, role 을 payload(claim) 에 담아 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .claim("username", authentication.getName())
                .claim("auth", authorities)
                .compact();

        // RefreshToken: AccessToken 만료 시 재발급 용도 - payload 최소화
        String refreshToken = Jwts.builder()
                .setSubject("Refresh_Token")
                .setExpiration(new Date(now + JWTProperties.REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 토큰 -> Authentication 객체 변환 (Filter 에서 SecurityContext 에 저장할 때 사용)
    public Authentication getAuthentication(String accessToken) throws ExpiredJwtException {

        // 토큰 파싱 -> payload(claims) 추출
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        String username = (String) claims.get("username");
        String auth = (String) claims.get("auth"); // "ROLE_USER"

        // role 문자열 -> GrantedAuthority 컬렉션 변환
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : auth.split(",")) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        if (!userRepository.existsById(username)) {
            return null;
        }

        UserDto userDto = UserDto.builder()
                .id(username)
                .role(UserRole.valueOf(auth.replace("ROLE_", "")))
                .build();

        PrincipalDetails principalDetails = new PrincipalDetails(userDto);

        return new UsernamePasswordAuthenticationToken(principalDetails, null, authorities);
    }

    // 토큰 유효성 검증 - 만료 시 ExpiredJwtException 던짐
    public boolean validateToken(String token) throws ExpiredJwtException {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("[ExpiredJwtException] {}", e.getMessage());
            throw new ExpiredJwtException(null, null, null);
        }
    }

    public Key getKey() {
        return this.key;
    }
}
