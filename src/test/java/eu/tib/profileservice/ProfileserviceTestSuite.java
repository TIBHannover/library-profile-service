package eu.tib.profileservice;

import eu.tib.profileservice.connector.DnbConnectorTest;
import eu.tib.profileservice.connector.MarcXml2DocumentConverterTest;
import eu.tib.profileservice.controller.HomeControllerTest;
import eu.tib.profileservice.controller.UserControllerTest;
import eu.tib.profileservice.repository.CategoryRepositoryTest;
import eu.tib.profileservice.repository.DocumentRepositoryTest;
import eu.tib.profileservice.repository.UserRepositoryTest;
import eu.tib.profileservice.service.CategoryServiceTest;
import eu.tib.profileservice.service.DocumentImportServiceTest;
import eu.tib.profileservice.util.DocumentAssignmentFinderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
    // Unit Tests
    DnbConnectorTest.class, MarcXml2DocumentConverterTest.class,
    HomeControllerTest.class, UserControllerTest.class,
    CategoryServiceTest.class, DocumentImportServiceTest.class,
    DocumentAssignmentFinderTest.class,

    // Integration Tests
    CategoryRepositoryTest.class, DocumentRepositoryTest.class,
    UserRepositoryTest.class
})
public class ProfileserviceTestSuite {

}
