package eu.tib.profileservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentAssignment;
import eu.tib.profileservice.domain.User;

public interface DocumentAssignmentRepository extends JpaRepository<DocumentAssignment, Long> {
	
	public DocumentAssignment findByDocument_Id(final Long documentId);
	public DocumentAssignment findByDocument(final Document document);
	public List<DocumentAssignment> findAllByAssignee(final User assignee);

}
