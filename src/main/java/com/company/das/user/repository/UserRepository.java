package com.company.das.user.repository;

import com.company.das.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User,Long> {

    Optional<User> findByEmpIdAndIsDeletedFalse(String empId);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    List<User> findByIsDeletedFalse();

    boolean existsByEmpId(String empId);

    boolean existsByEmail(String email);
}