package eu.tib.profileservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;

public interface DocumentService {
	
	public Page<Document> findAll(final Pageable pageable);
	public Page<Document> findAllByExample(final Document example, final Pageable pageable);
	public Document findById(final Long id);
	
	public Page<Document> retrieveDocumentsByUser(final User user, final Pageable pageable);
	public Document assignToUser(final Document document, final User user);
	public Document acceptDocument(final Long id);
	public Document rejectDocument(final Long id);

}
