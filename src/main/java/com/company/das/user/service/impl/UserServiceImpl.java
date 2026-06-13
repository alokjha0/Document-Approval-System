package com.company.das.user.service.impl;

import com.company.das.common.exception.ResourceNotFoundException;
import com.company.das.department.entity.Department;
import com.company.das.department.repository.DepartmentRepository;
import com.company.das.user.dto.UserDto;
import com.company.das.user.entity.User;
import com.company.das.user.repository.UserRepository;
import com.company.das.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void saveUser(UserDto dto) {

        String email = dto.getEmail().trim();
        String empId = dto.getEmpId().trim();

        // ✅ Duplicate checks
        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email)) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByEmpIdIgnoreCaseAndIsDeletedFalse(empId)) {
            throw new RuntimeException("Employee ID already exists");
        }

        // ✅ Fetch department
        Department department = departmentRepository
                .findByIdAndIsDeletedFalse(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // ✅ Create user
        User user = User.builder()
                .empId(empId)
                .name(dto.getName().trim())
                .email(email)
                .password(dto.getPassword().trim())
                .role(dto.getRole())
                .department(department)
                .build();

        userRepository.save(user);
    }

    @Override
    public void updateUser(Long id, UserDto dto) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newEmail = dto.getEmail().trim();
        String newEmpId = dto.getEmpId().trim();

        // ✅ Email duplicate check
        if (!user.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(newEmail)) {

            throw new RuntimeException("Email already exists");
        }

        // ✅ EmpId duplicate check
        if (!user.getEmpId().equalsIgnoreCase(newEmpId)
                && userRepository.existsByEmpIdIgnoreCaseAndIsDeletedFalse(newEmpId)) {

            throw new RuntimeException("Employee ID already exists");
        }

        // ✅ Fetch department
        Department department = departmentRepository
                .findByIdAndIsDeletedFalse(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        // ✅ Update fields
        user.setEmpId(newEmpId);
        user.setName(dto.getName().trim());
        user.setEmail(newEmail);
        user.setPassword(dto.getPassword().trim());
        user.setRole(dto.getRole());
        user.setDepartment(department);

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public UserDto getUserById(Long id) {

        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserDto.builder()
        	    .id(user.getId())
        	    .empId(user.getEmpId())
        	    .name(user.getName())
        	    .email(user.getEmail())
        	    .password(user.getPassword())
        	    .role(user.getRole())
        	    .departmentId(user.getDepartment().getId())
        	    .departmentName(user.getDepartment().getDepartmentName()) 
        	    .build();
    }

    @Override
    public Page<UserDto> getUsers(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<User> users;

        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository
                    .findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable);
        } else {
            users = userRepository.findByIsDeletedFalse(pageable);
        }

        return users.map(user -> UserDto.builder()
        	    .id(user.getId())
        	    .empId(user.getEmpId())
        	    .name(user.getName())
        	    .email(user.getEmail())
        	    .role(user.getRole())
        	    .departmentId(user.getDepartment().getId())
        	    .departmentName(user.getDepartment().getDepartmentName()) 
        	    .build());
    }
}