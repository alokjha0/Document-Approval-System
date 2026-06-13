package com.company.das.user.service;

import com.company.das.user.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {

    void saveUser(UserDto dto);

    void updateUser(Long id, UserDto dto);

    void deleteUser(Long id);

    UserDto getUserById(Long id);

    Page<UserDto> getUsers(String keyword, int page, int size);
}
