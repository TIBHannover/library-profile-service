package eu.tib.profileservice.connector;

import java.time.LocalDate;
import java.util.List;

import eu.tib.profileservice.domain.DocumentMetadata;

public interface InstitutionConnector {
	
	public List<DocumentMetadata> retrieveDocuments(final LocalDate from, final LocalDate to);

}
