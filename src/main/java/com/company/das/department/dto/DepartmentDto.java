package com.company.das.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {

    private Long id;

    @NotBlank(
            message = "Department name is required"
    )
    @Size(
            min = 1,
            max = 100,
            message = "Department name must be between 1 and 100 characters"
    )
    @Pattern(
            regexp = ".*\\S.*",
            message = "Department name cannot contain only spaces"
    )
    private String departmentName;

}