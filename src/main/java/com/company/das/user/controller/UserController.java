package com.company.das.user.controller;

import com.company.das.department.service.DepartmentService;
import com.company.das.user.dto.UserDto;
import com.company.das.user.entity.UserRole;
import com.company.das.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DepartmentService departmentService;

    @GetMapping
    public String userPage(@RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {

        Page<UserDto> users = userService.getUsers(keyword, page, 10);

        // ✅ Empty form object
        model.addAttribute("user", new UserDto());

        // ✅ Table data
        model.addAttribute("users", users);

        // ✅ Search value
        model.addAttribute("keyword", keyword);

        // ✅ ENUM dropdown
        model.addAttribute("roles", UserRole.values());

        // ✅ Department dropdown
        model.addAttribute("departments",
                departmentService.getDepartments("", 0, 100).getContent());

        return "user/index";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") UserDto dto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (result.hasErrors()) {

            model.addAttribute("users", userService.getUsers("", 0, 10));
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("departments",
                    departmentService.getDepartments("", 0, 100).getContent());

            return "user/index";
        }

        try {
            userService.saveUser(dto);
            redirectAttributes.addFlashAttribute("success", "User created successfully");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("users",
                    userService.getUsers(keyword, page, 10));

            model.addAttribute("user",
                    userService.getUserById(id));

            model.addAttribute("keyword", keyword);

            model.addAttribute("roles", UserRole.values());

            model.addAttribute("departments",
                    departmentService.getDepartments("", 0, 100).getContent());

            return "user/index";

        } catch (Exception ex) {

            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/users";
        }
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") UserDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (result.hasErrors()) {

            model.addAttribute("users", userService.getUsers("", 0, 10));
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("departments",
                    departmentService.getDepartments("", 0, 100).getContent());

            return "user/index";
        }

        try {
            userService.updateUser(id, dto);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {

        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully");

        return "redirect:/users";
    }
}
