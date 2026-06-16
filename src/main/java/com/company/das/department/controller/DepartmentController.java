package com.company.das.department.controller;

import com.company.das.department.dto.DepartmentDto;
import com.company.das.department.service.DepartmentService;
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
@RequestMapping("/departments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DepartmentController {

	private final DepartmentService departmentService;

	@GetMapping
	public String departmentPage(@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "0") int page, Model model) {

		Page<DepartmentDto> departments = departmentService.getDepartments(keyword, page, 10);

		model.addAttribute("department", new DepartmentDto());

		model.addAttribute("departments", departments);

		model.addAttribute("keyword", keyword);

		return "department/index";
	}

	@PostMapping("/save")
	public String saveDepartment(@Valid @ModelAttribute("department") DepartmentDto dto, BindingResult result,
			RedirectAttributes redirectAttributes, Model model) {

		if (result.hasErrors()) {

			Page<DepartmentDto> departments = departmentService.getDepartments("", 0, 10);

			model.addAttribute("departments", departments);
			return "department/index";
		}

		departmentService.saveDepartment(dto);

		redirectAttributes.addFlashAttribute("success", "Department created successfully");

		return "redirect:/departments";
	}

	@GetMapping("/edit/{id}")
	public String editDepartment(@PathVariable Long id, @RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "0") int page, Model model, RedirectAttributes redirectAttributes) {

		try {
			Page<DepartmentDto> departments = departmentService.getDepartments(keyword, page, 10);

			model.addAttribute("departments", departments);
			model.addAttribute("department", departmentService.getDepartmentById(id));
			model.addAttribute("keyword", keyword);

			return "department/index";

		} catch (Exception ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

			return "redirect:/departments";
		}
	}

	@PostMapping("/update/{id}")
	public String updateDepartment(@PathVariable Long id, @Valid @ModelAttribute("department") DepartmentDto dto,
			BindingResult result, RedirectAttributes redirectAttributes, Model model) {

		if (result.hasErrors()) {

			// reload table data
			Page<DepartmentDto> departments = departmentService.getDepartments("", 0, 10);

			model.addAttribute("departments", departments);
			model.addAttribute("keyword", "");

			return "department/index";
		}

		try {
			departmentService.updateDepartment(id, dto);

			redirectAttributes.addFlashAttribute("success", "Department updated successfully");

		} catch (Exception ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}

		return "redirect:/departments";
	}

	@GetMapping("/delete/{id}")
	public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {

		try {
			departmentService.deleteDepartment(id);

			redirectAttributes.addFlashAttribute("success", "Department deleted successfully");
		} catch (IllegalStateException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());

		}

		return "redirect:/departments";
	}
}