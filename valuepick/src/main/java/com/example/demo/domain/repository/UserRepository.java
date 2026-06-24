package com.example.demo.domain.repository;

import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    // JpaRepository<User, String>
    // - User   : 관리할 엔티티
    // - String : User의 PK 타입 (id 필드 = 이메일, String)
}
