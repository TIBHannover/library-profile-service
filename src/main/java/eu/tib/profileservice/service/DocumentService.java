package eu.tib.profileservice.service;

import java.util.List;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentAssignment;
import eu.tib.profileservice.domain.User;

public interface DocumentService {
	
	public List<Document> findAll();
	public Document findById(final Long id);
	
	public DocumentAssignment retrieveAssignmentByDocumentId(final Long documentId);
	
	public List<DocumentAssignment> retrieveDocumentAssignmentsByUser(final User user);
	public DocumentAssignment assignToUser(final Document document, final User user);
	public Document acceptDocument(final Long id);
	public Document rejectDocument(final Long id);

}
