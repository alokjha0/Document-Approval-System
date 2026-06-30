package com.company.das.comment.service.impl;

import com.company.das.comment.dto.DocumentCommentDto;
import com.company.das.comment.entity.DocumentComment;
import com.company.das.comment.repository.DocumentCommentRepository;
import com.company.das.comment.service.DocumentCommentService;
import com.company.das.common.enums.CommentType;
import com.company.das.document.entity.Document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DocumentCommentServiceImpl implements DocumentCommentService {

	private final DocumentCommentRepository documentCommentRepository;

	@Override
	public DocumentCommentDto getLatestComment(Long documentId, CommentType commentType)

	{

		Document document = Document.builder().id(documentId).build();

		DocumentComment comment = documentCommentRepository
				.findTopByDocumentAndCommentTypeOrderByCreatedAtDesc(document, commentType).orElse(null);

		if (comment == null) {

			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

		return DocumentCommentDto.builder().id(comment.getId()).comment(comment.getComment())
				.commentType(comment.getCommentType()).commentedBy(comment.getCommentedBy().getName())
				.stage(comment.getWorkflowTask().getStage().name())
				.department(comment.getWorkflowTask().getDepartment().getDepartmentName())
				.createdAt(comment.getCreatedAt().format(formatter)).build();

	}

}