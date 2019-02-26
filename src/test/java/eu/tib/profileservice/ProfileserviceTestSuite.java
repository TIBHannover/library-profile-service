package eu.tib.profileservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.tib.profileservice.connector.DNBConnectorTest;
import eu.tib.profileservice.connector.MarcXml2DocumentConverterTest;
import eu.tib.profileservice.repository.CategoryRepositoryTest;
import eu.tib.profileservice.repository.DocumentRepositoryTest;
import eu.tib.profileservice.repository.UserRepositoryTest;
import eu.tib.profileservice.service.CategoryServiceTest;
import eu.tib.profileservice.service.DocumentImportServiceTest;
import eu.tib.profileservice.util.DocumentAssignmentFinderTest;

@RunWith(Suite.class)
@SuiteClasses(value = {
		// Unit Tests
		DNBConnectorTest.class, MarcXml2DocumentConverterTest.class,
		CategoryServiceTest.class, DocumentImportServiceTest.class,
		DocumentAssignmentFinderTest.class,
		
		// Integration Tests
		CategoryRepositoryTest.class, DocumentRepositoryTest.class,
		UserRepositoryTest.class
		})
public class ProfileserviceTestSuite {

}
