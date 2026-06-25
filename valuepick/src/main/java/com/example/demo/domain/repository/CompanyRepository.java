package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    // 페이징 처리된 Company 전체 조회 - 대용량 데이터를 100건씩 나눠서 처리할 때 사용
    Page<Company> findAll(Pageable pageable);

}
