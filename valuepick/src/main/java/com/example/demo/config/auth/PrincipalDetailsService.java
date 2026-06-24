package com.example.demo.config.auth;

import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // 로그인 시 Security 가 자동으로 호출 - username(이메일)으로 DB 조회 후 PrincipalDetails 반환
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("PrincipalDetailsService loadUserByUsername: {}", username);

        Optional<User> userOptional = userRepository.findById(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException(username + " 존재하지 않는 계정입니다.");
        }

        User user = userOptional.get();
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .password(user.getPassword())
                .role(user.getRole())
                .build();

        return PrincipalDetails.builder()
                .userDto(userDto)
                .build();
    }
}
