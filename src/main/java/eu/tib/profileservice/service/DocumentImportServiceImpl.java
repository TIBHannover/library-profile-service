package eu.tib.profileservice.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.util.DocumentAssignmentFinder;

@Service
public class DocumentImportServiceImpl implements DocumentImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DocumentImportServiceImpl.class);
	
	@Autowired
	@Qualifier("DNBConnector")
	private InstitutionConnector dnbConn;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DocumentRepository documentRepository;


	@Override
	public void importDocuments(LocalDate from, LocalDate to) {
		DocumentAssignmentFinder documentAssignmentFinder = new DocumentAssignmentFinder(userService.findAll());
		
		List<DocumentMetadata> documents = dnbConn.retrieveDocuments(from, to);
		if (documents == null) {
			LOG.error("Cannot retrieve documents from DNB");
		} else {
			documents.forEach(doc->createNewDocument(doc, documentAssignmentFinder));
			// TODO assign
		}
	}
	
	/**
	 * Persist a new {@link Document} for the given {@link DocumentMetadata}, if not already existing.
	 * @param documentMetadata
	 */
	private void createNewDocument(final DocumentMetadata documentMetadata, final DocumentAssignmentFinder documentAssignmentFinder) {
		if (!isValid(documentMetadata)) {
			LOG.error("invalid document: " + buildDocumentMetadataString(documentMetadata));
			return;
		}
		Document existingDocument = findExistingDocument(documentMetadata);
		if (existingDocument != null) {
			LOG.debug("document already exists: " + buildDocumentMetadataString(documentMetadata));
		} else {
			Document document = new Document();
			document.setMetadata(documentMetadata);
			if (shouldIgnore(documentMetadata)) {
				document.setStatus(Status.IGNORED);
			} else {
				document.setStatus(Status.IN_PROGRESS);
				document.setAssignee(documentAssignmentFinder.determineAssignee(documentMetadata));
			}
			document = save(document);
			LOG.debug("document imported: " + buildDocumentMetadataString(documentMetadata));
		}
	}
	
	@Transactional
	private Document save(final Document document) {
		return documentRepository.save(document);
	}
	
	/**
	 * Check if the given document should be ignored
	 * @param documentMetadata
	 * @return true, if the document should be ignored; false otherwise
	 */
	private boolean shouldIgnore(final DocumentMetadata documentMetadata) {
		// TODO rules (ignore new documents)
		return false;
	}
	
	/**
	 * Find the already existing {@link Document} that matches the given {@link DocumentMetadata}
	 * @param documentMetadata
	 * @return the {@link Document}, if existing; null otherwise
	 */
	private Document findExistingDocument(final DocumentMetadata documentMetadata) {
		for (String isbn : documentMetadata.getIsbns()) {
			Document existingDocument = documentRepository.findByMetadataIsbns(isbn);
			if (existingDocument != null) {
				return existingDocument;
			}
		}
		return null;
	}
	
	private String buildDocumentMetadataString(final DocumentMetadata documentMetadata) {
		final StringBuilder sb = new StringBuilder();
		sb.append(documentMetadata.getTitle());
		sb.append(", ").append(documentMetadata.getRemainderOfTitle());
		sb.append(", ").append(documentMetadata.getAuthor());
		sb.append(", ").append(documentMetadata.getIsbns());
		return sb.toString();
	}
	
	private boolean isValid(final DocumentMetadata documentMetadata) {
		// TODO
		boolean valid = documentMetadata.getIsbns() != null && documentMetadata.getIsbns().size() > 0;
		
		
		return valid;
	}

}
