package eu.tib.profileservice.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentAssignment;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.DocumentAssignmentRepository;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.repository.UserRepository;

@Service
public class DocumentServiceImpl implements DocumentService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private DocumentRepository documentRepository;
	@Autowired
	private DocumentAssignmentRepository documentAssignmentRepository;

	@Transactional(readOnly=true)
	@Override
	public List<Document> findAll() {
		return documentRepository.findAll();
	}

	@Transactional(readOnly=true)
	@Override
	public Document findById(final Long id) {
		if (id == null) {
			return null;
		} 
		try {
			return documentRepository.getOne(id);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	@Transactional(readOnly=true)
	@Override
	public DocumentAssignment retrieveAssignmentByDocumentId(final Long documentId) {
		if (documentId == null) {
			return null;
		}
		try {
			return documentAssignmentRepository.findByDocument_Id(documentId);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}


	@Transactional(readOnly=true)
	@Override
	public List<DocumentAssignment> retrieveDocumentAssignmentsByUser(final User user) {
		return documentAssignmentRepository.findAllByAssignee(user);
	}

	@Transactional
	@Override
	public DocumentAssignment assignToUser(final Document document, final User user) {
		if (document == null || user == null || user.getId() == null) {
			LOG.debug("document/user cannot be null");
			return null;
		}
		final Document persistedDocument = findById(document.getId());
		final User persistedUser = userRepository.getOne(user.getId());
		if (persistedDocument == null || persistedUser == null) {
			LOG.debug("cannot find document/user");
			return null;
		}
		DocumentAssignment assignment = documentAssignmentRepository.findByDocument(document);
		if (assignment == null) {
			assignment = new DocumentAssignment();
			assignment.setDocument(persistedDocument);
		}
		assignment.setAssignee(persistedUser);
		return documentAssignmentRepository.save(assignment);
	}

	@Transactional
	@Override
	public Document acceptDocument(Long id) {
		if (id == null) {
			return null;
		} 
		final Document document = documentRepository.getOne(id);
		document.setStatus(Status.ACCEPTED);
		return documentRepository.save(document);
	}

	@Transactional
	@Override
	public Document rejectDocument(Long id) {
		if (id == null) {
			return null;
		} 
		final Document document = documentRepository.getOne(id);
		document.setStatus(Status.REJECTED);
		return documentRepository.save(document);
	}

}
