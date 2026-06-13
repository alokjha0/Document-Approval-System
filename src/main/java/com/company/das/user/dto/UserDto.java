package com.company.das.user.dto;

import com.company.das.user.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank(message = "Employee ID is required")
    @Size(min = 2, max = 20, message = "Employee ID must be between 2 and 20 characters")
    private String empId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 3, max = 100, message = "Password must be at least 3 characters")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Department ID is required")
    private Long departmentId;
    
    private String departmentName;
}
