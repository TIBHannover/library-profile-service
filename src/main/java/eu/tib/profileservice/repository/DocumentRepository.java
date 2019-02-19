package eu.tib.profileservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	public Document findByMetadataIsbns(final String isbn);
	public List<Document> findAllByAssignee(final User assignee);
}
