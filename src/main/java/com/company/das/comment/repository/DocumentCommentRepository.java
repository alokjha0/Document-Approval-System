package com.company.das.comment.repository;

import com.company.das.comment.entity.DocumentComment;
import com.company.das.common.enums.CommentType;
import com.company.das.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

	List<DocumentComment> findByDocumentOrderByCreatedAtAsc(Document document);

//	DocumentComment findTopByDocumentOrderByCreatedAtDesc(Document document);

	DocumentComment findTopByDocumentIdOrderByCreatedAtDesc(Long documentId);

	Optional<DocumentComment> findTopByDocumentAndCommentTypeOrderByCreatedAtDesc(Document document,
			CommentType commentType);
}