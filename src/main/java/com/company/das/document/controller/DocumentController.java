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
	public String respondToInfoRequest(
	        @PathVariable Long id,
	        Authentication authentication) {

	    documentService.respondToInfoRequest(
	            id,
	            authentication.getName());

	    return "redirect:/documents";
	}
	
	@GetMapping("/respond/approver/{id}")
	public String respondToInfoRequestByApprover(
	        @PathVariable Long id,
	        Authentication authentication) {

	    documentService.respondToInfoRequestByApprover(
	            id,
	            authentication.getName());

	    return "redirect:/documents";
	}
	@GetMapping("/respond/senior-approver/{id}")
	public String respondToInfoRequestBySeniorApprover(
	        @PathVariable Long id,
	        Authentication authentication) {

	    documentService.respondToInfoRequestBySeniorApprover(
	            id,
	            authentication.getName());

	    return "redirect:/documents";
	}
}