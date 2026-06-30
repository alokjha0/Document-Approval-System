package com.company.das.document.controller;

import com.company.das.department.service.DepartmentService;
import com.company.das.document.dto.DocumentDto;
import com.company.das.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;

	private final DepartmentService departmentService;

	@GetMapping
	public String documentPage(Model model, Authentication authentication) {

		model.addAttribute("documentDto", new DocumentDto());

		model.addAttribute("departments", departmentService.getAllDepartments());

		model.addAttribute("documents", documentService.getDocumentsByOwner(authentication.getName()));

		return "document/index";
	}

	@GetMapping("/edit/{id}")
	public String editDocument(@PathVariable Long id, Model model, Authentication authentication) {

		model.addAttribute("documentDto", documentService.getDocumentById(id));

		model.addAttribute("departments", departmentService.getAllDepartments());

		model.addAttribute("documents", documentService.getDocumentsByOwner(authentication.getName()));

		return "document/index";
	}

	@PostMapping("/update/{id}")
	public String updateDocument(@PathVariable Long id, @Valid @ModelAttribute("documentDto") DocumentDto documentDto,
			BindingResult result, Authentication authentication, Model model) {

		if (result.hasErrors()) {

			model.addAttribute("departments", departmentService.getAllDepartments());

			model.addAttribute("documents", documentService.getDocumentsByOwner(authentication.getName()));

			return "document/index";
		}

		documentService.updateDocument(id, documentDto, authentication.getName());

		return "redirect:/documents";
	}

	@GetMapping("/submit/{id}")
	public String submitDocument(@PathVariable Long id, Authentication authentication) {

		documentService.submitDocument(id, authentication.getName());

		return "redirect:/documents";
	}

	@PostMapping("/save")
	public String saveDocument(@Valid @ModelAttribute("documentDto") DocumentDto documentDto, BindingResult result,
			Authentication authentication, Model model) {

		if (result.hasErrors()) {

			model.addAttribute("departments", departmentService.getAllDepartments());

			model.addAttribute("documents", documentService.getDocumentsByOwner(authentication.getName()));

			return "document/index";
		}

		documentService.createDocument(documentDto, authentication.getName());

		return "redirect:/documents";
	}

	@GetMapping("/respond/{id}")
	public String respondToInfoRequest(@PathVariable Long id, Authentication authentication, Model model) {

		model.addAttribute("documentDto", documentService.getDocumentDetails(id, authentication.getName()));

		return "document/respond";
	}
	
	@PostMapping("/respond/{id}")
	public String submitResponse(
	        @PathVariable Long id,
	        @ModelAttribute("documentDto") DocumentDto documentDto,
	        Authentication authentication) {

	    documentService.submitResponse(
	            id,
	            documentDto,
	            authentication.getName());

	    return "redirect:/documents";
	}

	

	@GetMapping("/view/{id}")
	public String viewDocument(@PathVariable Long id, Authentication authentication, Model model) {

		model.addAttribute("document", documentService.getDocumentDetails(id, authentication.getName()));

		return "document/view";
	}
}