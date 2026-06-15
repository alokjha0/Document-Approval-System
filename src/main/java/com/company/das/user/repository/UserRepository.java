package com.company.das.user.repository;

import com.company.das.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailAndIsDeletedFalse(String email);
	
    // ✅ Get all non-deleted users (pagination)
    Page<User> findByIsDeletedFalse(Pageable pageable);

    // ✅ Search by name (case-insensitive + non-deleted)
    Page<User> findByNameContainingIgnoreCaseAndIsDeletedFalse(String keyword, Pageable pageable);

    // ✅ Get single user (non-deleted)
    Optional<User> findByIdAndIsDeletedFalse(Long id);

    // ✅ Duplicate checks
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);

    boolean existsByEmpIdIgnoreCase(String empId);

    boolean existsByEmpIdIgnoreCaseAndIsDeletedFalse(String empId);
}