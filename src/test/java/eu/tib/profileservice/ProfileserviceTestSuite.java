package eu.tib.profileservice;

import eu.tib.profileservice.connector.BlConnectorTest;
import eu.tib.profileservice.connector.DnbConnectorTest;
import eu.tib.profileservice.connector.InstitutionConnectorFactoryTest;
import eu.tib.profileservice.connector.MarcXml2DocumentConverterTest;
import eu.tib.profileservice.connector.RdfXml2DocumentConverterTest;
import eu.tib.profileservice.connector.TibConnectorTest;
import eu.tib.profileservice.controller.DocumentControllerTest;
import eu.tib.profileservice.controller.HomeControllerTest;
import eu.tib.profileservice.controller.ImportControllerTest;
import eu.tib.profileservice.controller.UserControllerTest;
import eu.tib.profileservice.repository.CategoryRepositoryTest;
import eu.tib.profileservice.repository.DocumentImportStatisticsRepositoryTest;
import eu.tib.profileservice.repository.DocumentRepositoryTest;
import eu.tib.profileservice.repository.ImportFilterRepositoryTest;
import eu.tib.profileservice.repository.UserRepositoryTest;
import eu.tib.profileservice.scheduling.DocumentCleanupJobTest;
import eu.tib.profileservice.scheduling.DocumentImportJobTest;
import eu.tib.profileservice.service.CategoryServiceTest;
import eu.tib.profileservice.service.DocumentImportServiceTest;
import eu.tib.profileservice.service.DocumentServiceTest;
import eu.tib.profileservice.service.ImportFilterServiceTest;
import eu.tib.profileservice.service.UserDetailsServiceTest;
import eu.tib.profileservice.service.UserServiceTest;
import eu.tib.profileservice.util.DocumentAssignmentFinderTest;
import eu.tib.profileservice.util.ImportFilterProcessorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
    // Unit Tests
    DnbConnectorTest.class, MarcXml2DocumentConverterTest.class,
    BlConnectorTest.class, RdfXml2DocumentConverterTest.class,
    InstitutionConnectorFactoryTest.class, TibConnectorTest.class,
    HomeControllerTest.class, UserControllerTest.class,
    DocumentControllerTest.class, ImportControllerTest.class,
    DocumentCleanupJobTest.class, DocumentImportJobTest.class,
    CategoryServiceTest.class, DocumentImportServiceTest.class,
    DocumentServiceTest.class, ImportFilterServiceTest.class,
    UserDetailsServiceTest.class, UserServiceTest.class,
    DocumentAssignmentFinderTest.class, ImportFilterProcessorTest.class,

    // Integration Tests
    CategoryRepositoryTest.class, DocumentRepositoryTest.class,
    ImportFilterRepositoryTest.class, UserRepositoryTest.class,
    DocumentImportStatisticsRepositoryTest.class
})
public class ProfileserviceTestSuite {

}
