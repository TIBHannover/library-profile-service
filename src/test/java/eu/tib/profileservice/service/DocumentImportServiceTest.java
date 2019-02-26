package eu.tib.profileservice.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.repository.DocumentRepository;

@RunWith(SpringRunner.class)
public class DocumentImportServiceTest {
	
    @TestConfiguration
    static class TestContextConfiguration {
  
        @Bean
        public DocumentImportService service() {
            return new DocumentImportServiceImpl();
        }
    }

	@Autowired
	private DocumentImportService documentImportService;
	@MockBean
	private DocumentRepository documentRepository;
	@MockBean(name="DNBConnector")
	private InstitutionConnector dnbConn;
	@MockBean
	private UserService userService;

	private DocumentMetadata newDocumentMetadataDummy() {
		final DocumentMetadata document = new DocumentMetadata();		
		document.setTitle("title");		
		document.setRemainderOfTitle("remainderOfTitle");
		document.setIsbns(Arrays.asList(new String[] {"1234567890"}));
		return document;
	}

	@Test
	public void testImportDocumentsWithInvalidResult() {
		DocumentMetadata invalidMetadata = newDocumentMetadataDummy();
		invalidMetadata.setIsbns(null);
		List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {invalidMetadata});
		when(dnbConn.retrieveDocuments(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class))).thenReturn(connectorResult);
		OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
		LocalDate now = utc.toLocalDate();		
		documentImportService.importDocuments(now, now);
		
		verify(documentRepository, times(0)).save(Mockito.any(Document.class));
	}

	@Test
	public void testImportDocumentsWithNullResult() {
		when(dnbConn.retrieveDocuments(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class))).thenReturn(null);
		OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
		LocalDate now = utc.toLocalDate();		
		documentImportService.importDocuments(now, now);
		
		verify(documentRepository, times(0)).save(Mockito.any(Document.class));
	}
	
	@Test
	public void testImportDocumentsWithoutExistingDocument() {
		List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {newDocumentMetadataDummy()});
		when(dnbConn.retrieveDocuments(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class))).thenReturn(connectorResult);
		when(documentRepository.findByMetadataIsbns(Mockito.anyString())).thenReturn(null);
		
		OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
		LocalDate now = utc.toLocalDate();		
		documentImportService.importDocuments(now, now);

		verify(documentRepository, times(1)).save(Mockito.any(Document.class));
	}

	@Test
	public void testImportDocumentsWithExistingDocument() {
		Document existingDocument = new Document();
		existingDocument.setMetadata(newDocumentMetadataDummy());
		List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {newDocumentMetadataDummy()});
		when(dnbConn.retrieveDocuments(Mockito.any(LocalDate.class), Mockito.any(LocalDate.class))).thenReturn(connectorResult);
		when(documentRepository.findByMetadataIsbns(Mockito.anyString())).thenReturn(existingDocument);
		
		OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
		LocalDate now = utc.toLocalDate();		
		documentImportService.importDocuments(now, now);

		verify(documentRepository, times(0)).save(Mockito.any(Document.class));
	}

}
